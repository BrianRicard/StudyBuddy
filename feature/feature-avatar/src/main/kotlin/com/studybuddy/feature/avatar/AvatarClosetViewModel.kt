package com.studybuddy.feature.avatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.usecase.avatar.GetAvatarConfigUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseItemUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseResult
import com.studybuddy.core.domain.usecase.avatar.UpdateAvatarUseCase
import com.studybuddy.core.domain.usecase.points.GetTotalPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Avatar Closet screen.
 */
data class AvatarClosetState(
    val avatarConfig: AvatarConfig? = null,
    val selectedTab: AccessoryTab = AccessoryTab.HATS,
    val ownedItemIds: Set<String> = emptySet(),
    val starBalance: Long = 0L,
    val showPurchaseDialog: RewardItem? = null,
    val purchaseError: String? = null,
    val isLoading: Boolean = true,
)

/**
 * Accessory tab categories displayed in the Avatar Closet.
 */
enum class AccessoryTab(val label: String, val icon: String) {
    HATS("Hats", "\uD83C\uDFA9"),
    FACE("Face", "\uD83D\uDD76\uFE0F"),
    OUTFIT("Outfit", "\uD83D\uDC54"),
    PETS("Pets", "\uD83D\uDC3E"),
}

/**
 * User actions dispatched to the Avatar Closet ViewModel.
 */
sealed interface AvatarClosetIntent {
    data class SelectCharacter(val bodyId: String) : AvatarClosetIntent
    data class SelectTab(val tab: AccessoryTab) : AvatarClosetIntent
    data class EquipItem(val itemId: String) : AvatarClosetIntent
    data class RequestPurchase(val item: RewardItem) : AvatarClosetIntent
    data object ConfirmPurchase : AvatarClosetIntent
    data object DismissPurchaseDialog : AvatarClosetIntent
}

/**
 * One-shot side effects emitted by the Avatar Closet ViewModel.
 */
sealed interface AvatarClosetEffect {
    data class PurchaseSuccess(val itemName: String) : AvatarClosetEffect
    data class ShowError(val message: String) : AvatarClosetEffect
}

@HiltViewModel
class AvatarClosetViewModel @Inject constructor(
    private val getAvatarConfigUseCase: GetAvatarConfigUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val purchaseItemUseCase: PurchaseItemUseCase,
    private val getTotalPointsUseCase: GetTotalPointsUseCase,
    private val rewardsRepository: RewardsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AvatarClosetState())
    val state: StateFlow<AvatarClosetState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AvatarClosetEffect>()
    val effects: SharedFlow<AvatarClosetEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    init {
        observeData()
    }

    fun onIntent(intent: AvatarClosetIntent) {
        when (intent) {
            is AvatarClosetIntent.SelectCharacter -> selectCharacter(intent.bodyId)
            is AvatarClosetIntent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.tab) }
            }
            is AvatarClosetIntent.EquipItem -> equipItem(intent.itemId)
            is AvatarClosetIntent.RequestPurchase -> {
                _state.update {
                    it.copy(showPurchaseDialog = intent.item, purchaseError = null)
                }
            }
            is AvatarClosetIntent.ConfirmPurchase -> confirmPurchase()
            is AvatarClosetIntent.DismissPurchaseDialog -> {
                _state.update {
                    it.copy(showPurchaseDialog = null, purchaseError = null)
                }
            }
        }
    }

    /**
     * Combines avatar config, owned rewards, and star balance into a single
     * reactive stream that keeps the UI state in sync.
     */
    private fun observeData() {
        viewModelScope.launch {
            combine(
                getAvatarConfigUseCase(profileId).filterNotNull(),
                rewardsRepository.getOwnedRewards(profileId),
                getTotalPointsUseCase(profileId),
            ) { config, ownedRewards, balance ->
                val ownedIds = ownedRewards.map { it.id }.toSet() +
                    RewardCatalog.starterItemIds
                Triple(config, ownedIds, balance)
            }.collect { (config, ownedIds, balance) ->
                _state.update {
                    it.copy(
                        avatarConfig = config,
                        ownedItemIds = ownedIds,
                        starBalance = balance,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun selectCharacter(bodyId: String) {
        val currentConfig = _state.value.avatarConfig ?: return
        val updatedConfig = currentConfig.copy(bodyId = bodyId)
        _state.update { it.copy(avatarConfig = updatedConfig) }
        viewModelScope.launch {
            updateAvatarUseCase(profileId = profileId, config = updatedConfig)
        }
    }

    private fun equipItem(itemId: String) {
        val currentConfig = _state.value.avatarConfig ?: return
        val item = RewardCatalog.getItemById(itemId) ?: return
        val updatedConfig = applyItemToConfig(config = currentConfig, item = item)
        _state.update { it.copy(avatarConfig = updatedConfig) }
        viewModelScope.launch {
            updateAvatarUseCase(profileId = profileId, config = updatedConfig)
        }
    }

    private fun confirmPurchase() {
        val item = _state.value.showPurchaseDialog ?: return
        viewModelScope.launch {
            when (val result = purchaseItemUseCase(profileId = profileId, item = item)) {
                is PurchaseResult.Success -> {
                    val currentConfig = _state.value.avatarConfig ?: return@launch
                    val updatedConfig = applyItemToConfig(
                        config = currentConfig,
                        item = item,
                    )
                    _state.update {
                        it.copy(
                            avatarConfig = updatedConfig,
                            showPurchaseDialog = null,
                            purchaseError = null,
                        )
                    }
                    updateAvatarUseCase(
                        profileId = profileId,
                        config = updatedConfig,
                    )
                    _effects.emit(AvatarClosetEffect.PurchaseSuccess(item.name))
                }
                is PurchaseResult.InsufficientPoints -> {
                    _state.update {
                        it.copy(
                            purchaseError = "You need ${result.needed} more stars!",
                        )
                    }
                }
            }
        }
    }

    /**
     * Maps a reward item to the appropriate field in [AvatarConfig] based
     * on its [RewardCategory]. Categories outside avatar accessories are
     * ignored (themes, effects, sounds, titles are handled by Rewards Shop).
     */
    private fun applyItemToConfig(
        config: AvatarConfig,
        item: RewardItem,
    ): AvatarConfig = when (item.category) {
        RewardCategory.HAT -> config.copy(hatId = item.id)
        RewardCategory.FACE -> config.copy(faceId = item.id)
        RewardCategory.OUTFIT -> config.copy(outfitId = item.id)
        RewardCategory.PET -> config.copy(petId = item.id)
        else -> config
    }
}
