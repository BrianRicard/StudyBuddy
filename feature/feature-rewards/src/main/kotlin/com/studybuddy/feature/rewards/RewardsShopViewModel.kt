package com.studybuddy.feature.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.avatar.PurchaseItemUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseResult
import com.studybuddy.core.domain.usecase.points.GetTotalPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the currently selected tab in the Rewards Shop.
 */
enum class RewardsTab {
    AVATAR,
    THEMES,
    EFFECTS,
    TITLES,
}

/**
 * UI state for the Rewards Shop screen.
 */
data class RewardsShopState(
    val selectedTab: RewardsTab = RewardsTab.AVATAR,
    val ownedItemIds: Set<String> = emptySet(),
    val starBalance: Long = 0L,
    val activeTheme: String = "sunset",
    val equippedTitle: String? = null,
    val showPurchaseDialog: RewardItem? = null,
    val purchaseError: String? = null,
    val isLoading: Boolean = true,
)

/**
 * User actions dispatched to the Rewards Shop ViewModel.
 */
sealed interface RewardsShopIntent {
    data class SelectTab(val tab: RewardsTab) : RewardsShopIntent
    data class RequestPurchase(val item: RewardItem) : RewardsShopIntent
    data object ConfirmPurchase : RewardsShopIntent
    data object DismissDialog : RewardsShopIntent
    data class ActivateTheme(val themeId: String) : RewardsShopIntent
    data class EquipTitle(val titleId: String) : RewardsShopIntent
}

/**
 * One-shot side effects emitted by the Rewards Shop ViewModel.
 */
sealed interface RewardsShopEffect {
    data class PurchaseSuccess(val itemName: String) : RewardsShopEffect
    data class ThemeChanged(val themeId: String) : RewardsShopEffect
}

@HiltViewModel
class RewardsShopViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val settingsRepository: SettingsRepository,
    private val purchaseItemUseCase: PurchaseItemUseCase,
    private val getTotalPointsUseCase: GetTotalPointsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RewardsShopState())
    val state: StateFlow<RewardsShopState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RewardsShopEffect>()
    val effects: SharedFlow<RewardsShopEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    init {
        observeOwnedRewards()
        observeStarBalance()
        observeSelectedTheme()
    }

    fun onIntent(intent: RewardsShopIntent) {
        when (intent) {
            is RewardsShopIntent.SelectTab -> handleSelectTab(intent.tab)
            is RewardsShopIntent.RequestPurchase -> handleRequestPurchase(intent.item)
            is RewardsShopIntent.ConfirmPurchase -> handleConfirmPurchase()
            is RewardsShopIntent.DismissDialog -> handleDismissDialog()
            is RewardsShopIntent.ActivateTheme -> handleActivateTheme(intent.themeId)
            is RewardsShopIntent.EquipTitle -> handleEquipTitle(intent.titleId)
        }
    }

    private fun observeOwnedRewards() {
        viewModelScope.launch {
            rewardsRepository.getOwnedRewards(profileId).collect { ownedItems ->
                val ownedIds = ownedItems.map { it.id }.toSet() + RewardCatalog.starterItemIds
                _state.update { it.copy(ownedItemIds = ownedIds, isLoading = false) }
            }
        }
    }

    private fun observeStarBalance() {
        viewModelScope.launch {
            getTotalPointsUseCase(profileId).collect { balance ->
                _state.update { it.copy(starBalance = balance) }
            }
        }
    }

    private fun observeSelectedTheme() {
        viewModelScope.launch {
            settingsRepository.getSelectedTheme().collect { themeId ->
                _state.update { it.copy(activeTheme = themeId) }
            }
        }
    }

    private fun handleSelectTab(tab: RewardsTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    private fun handleRequestPurchase(item: RewardItem) {
        _state.update { it.copy(showPurchaseDialog = item, purchaseError = null) }
    }

    private fun handleConfirmPurchase() {
        val item = _state.value.showPurchaseDialog ?: return

        viewModelScope.launch {
            val result = purchaseItemUseCase(profileId = profileId, item = item)
            when (result) {
                is PurchaseResult.Success -> {
                    _state.update { it.copy(showPurchaseDialog = null, purchaseError = null) }
                    _effects.emit(RewardsShopEffect.PurchaseSuccess(item.name))
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

    private fun handleDismissDialog() {
        _state.update { it.copy(showPurchaseDialog = null, purchaseError = null) }
    }

    private fun handleActivateTheme(themeId: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedTheme(themeId)
            _effects.emit(RewardsShopEffect.ThemeChanged(themeId))
        }
    }

    private fun handleEquipTitle(titleId: String) {
        _state.update { it.copy(equippedTitle = titleId) }
    }
}
