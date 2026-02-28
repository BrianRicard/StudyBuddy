package com.studybuddy.feature.avatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.RewardCatalog
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Avatar Closet screen.
 */
data class AvatarClosetState(
    val avatarConfig: AvatarConfig? = null,
    val ownedItemIds: Set<String> = emptySet(),
    val starBalance: Long = 0L,
    val showPurchaseDialog: RewardItem? = null,
    val purchaseError: String? = null,
    val isLoading: Boolean = true,
)

/**
 * User actions dispatched to the Avatar Closet ViewModel.
 */
sealed interface AvatarClosetIntent {
    data class SelectCharacter(val bodyId: String) : AvatarClosetIntent
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
                getAvatarConfigUseCase(profileId),
                rewardsRepository.getOwnedRewards(profileId),
                getTotalPointsUseCase(profileId),
            ) { config, ownedRewards, balance ->
                @Suppress("NAME_SHADOWING")
                val config = config ?: AvatarConfig.default()
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
        val ownedIds = _state.value.ownedItemIds

        // Check if character is owned
        if (!RewardCatalog.isCharacterOwned(bodyId, ownedIds)) {
            // Not owned — show purchase dialog
            val item = RewardCatalog.getCharacterItem(bodyId) ?: return
            _state.update {
                it.copy(showPurchaseDialog = item, purchaseError = null)
            }
            return
        }

        // Owned — equip the character
        val updatedConfig = currentConfig.copy(bodyId = bodyId)
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
                    _state.update {
                        it.copy(
                            showPurchaseDialog = null,
                            purchaseError = null,
                        )
                    }
                    // If it's a character, auto-equip it
                    if (item.id.startsWith("char_")) {
                        val bodyId = item.id.removePrefix("char_")
                        val currentConfig = _state.value.avatarConfig ?: return@launch
                        val updatedConfig = currentConfig.copy(bodyId = bodyId)
                        _state.update { it.copy(avatarConfig = updatedConfig) }
                        updateAvatarUseCase(
                            profileId = profileId,
                            config = updatedConfig,
                        )
                    }
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
}
