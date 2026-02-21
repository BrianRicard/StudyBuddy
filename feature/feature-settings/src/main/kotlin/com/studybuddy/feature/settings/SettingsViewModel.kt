package com.studybuddy.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.BackupRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Settings screen.
 */
data class SettingsState(
    val profileName: String = "",
    val avatarConfig: AvatarConfig? = null,
    val locale: String = "en",
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val dailyGoal: Int = 5,
    val isAccentStrict: Boolean = false,
    val selectedTheme: String = "sunset",
    val showParentZone: Boolean = false,
    val parentPinSet: Boolean = false,
    val showPinDialog: Boolean = false,
    val pinError: String? = null,
    val showResetDialog: Boolean = false,
    val isLoading: Boolean = true,
)

/**
 * User actions dispatched to the Settings ViewModel.
 */
sealed interface SettingsIntent {
    data class SetLocale(val locale: String) : SettingsIntent
    data object ToggleSound : SettingsIntent
    data object ToggleHaptic : SettingsIntent
    data class SetDailyGoal(val goal: Int) : SettingsIntent
    data object ToggleAccentStrict : SettingsIntent
    data object OpenParentZone : SettingsIntent
    data class SubmitPin(val pin: String) : SettingsIntent
    data class SetNewPin(val pin: String) : SettingsIntent
    data object DismissPinDialog : SettingsIntent
    data object RequestReset : SettingsIntent
    data class ConfirmReset(val confirmText: String) : SettingsIntent
    data object DismissResetDialog : SettingsIntent
    data object NavigateToAvatarCloset : SettingsIntent
    data object NavigateToStats : SettingsIntent
    data object NavigateToBackup : SettingsIntent
}

/**
 * One-shot side effects emitted by the Settings ViewModel.
 */
sealed interface SettingsEffect {
    data class NavigateTo(val route: String) : SettingsEffect
    data class ShowToast(val message: String) : SettingsEffect
    data object AppReset : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val avatarRepository: AvatarRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    // TODO: Replace with actual profile ID from session/navigation args
    private val profileId = "default"

    /**
     * Stored parent PIN hash. Null means no PIN has been set yet.
     */
    private var storedPinHash: Int? = null

    init {
        observeSettings()
        observeProfile()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetLocale -> setLocale(intent.locale)
            is SettingsIntent.ToggleSound -> toggleSound()
            is SettingsIntent.ToggleHaptic -> toggleHaptic()
            is SettingsIntent.SetDailyGoal -> setDailyGoal(intent.goal)
            is SettingsIntent.ToggleAccentStrict -> toggleAccentStrict()
            is SettingsIntent.OpenParentZone -> openParentZone()
            is SettingsIntent.SubmitPin -> submitPin(intent.pin)
            is SettingsIntent.SetNewPin -> setNewPin(intent.pin)
            is SettingsIntent.DismissPinDialog -> dismissPinDialog()
            is SettingsIntent.RequestReset -> requestReset()
            is SettingsIntent.ConfirmReset -> confirmReset(intent.confirmText)
            is SettingsIntent.DismissResetDialog -> dismissResetDialog()
            is SettingsIntent.NavigateToAvatarCloset -> navigateTo(StudyBuddyRoutes.AVATAR)
            is SettingsIntent.NavigateToStats -> navigateTo(StudyBuddyRoutes.STATS)
            is SettingsIntent.NavigateToBackup -> navigateTo(StudyBuddyRoutes.BACKUP)
        }
    }

    /**
     * Combines all settings flows into a single reactive stream that keeps
     * the UI state in sync with DataStore changes.
     */
    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getAppLocale(),
                settingsRepository.isSoundEnabled(),
                settingsRepository.isHapticEnabled(),
                settingsRepository.getDailyGoal(),
                settingsRepository.isAccentStrict(),
                settingsRepository.getSelectedTheme(),
            ) { locale, sound, haptic, goal, accent, theme ->
                SettingsData(
                    locale = locale,
                    isSoundEnabled = sound,
                    isHapticEnabled = haptic,
                    dailyGoal = goal,
                    isAccentStrict = accent,
                    selectedTheme = theme,
                )
            }.collect { data ->
                _state.update {
                    it.copy(
                        locale = data.locale,
                        isSoundEnabled = data.isSoundEnabled,
                        isHapticEnabled = data.isHapticEnabled,
                        dailyGoal = data.dailyGoal,
                        isAccentStrict = data.isAccentStrict,
                        selectedTheme = data.selectedTheme,
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * Observes the active profile and its avatar configuration to display
     * in the profile card at the top of Settings.
     */
    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                profileRepository.getActiveProfile(),
                avatarRepository.getAvatarConfig(profileId),
            ) { profile, avatarConfig ->
                Pair(profile, avatarConfig)
            }.collect { (profile, avatarConfig) ->
                _state.update {
                    it.copy(
                        profileName = profile?.name ?: "",
                        avatarConfig = avatarConfig ?: profile?.avatarConfig,
                    )
                }
            }
        }
    }

    private fun setLocale(locale: String) {
        viewModelScope.launch {
            settingsRepository.setAppLocale(locale)
        }
    }

    private fun toggleSound() {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(!_state.value.isSoundEnabled)
        }
    }

    private fun toggleHaptic() {
        viewModelScope.launch {
            settingsRepository.setHapticEnabled(!_state.value.isHapticEnabled)
        }
    }

    private fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyGoal(goal)
        }
    }

    private fun toggleAccentStrict() {
        viewModelScope.launch {
            settingsRepository.setAccentStrict(!_state.value.isAccentStrict)
        }
    }

    /**
     * Opens the Parent Zone. If a PIN has been set, shows the PIN dialog
     * for verification. If no PIN exists, shows the dialog to create one.
     */
    private fun openParentZone() {
        if (_state.value.showParentZone) {
            // Already unlocked — toggle off
            _state.update { it.copy(showParentZone = false) }
            return
        }
        if (storedPinHash != null) {
            // PIN set — require verification
            _state.update {
                it.copy(showPinDialog = true, pinError = null)
            }
        } else {
            // No PIN yet — prompt to create one
            _state.update {
                it.copy(
                    showPinDialog = true,
                    pinError = null,
                    parentPinSet = false,
                )
            }
        }
    }

    /**
     * Verifies the entered PIN against the stored hash.
     */
    private fun submitPin(pin: String) {
        if (pin.length != PIN_LENGTH) {
            _state.update { it.copy(pinError = "PIN must be $PIN_LENGTH digits") }
            return
        }
        if (pin.hashCode() == storedPinHash) {
            _state.update {
                it.copy(
                    showParentZone = true,
                    showPinDialog = false,
                    pinError = null,
                )
            }
        } else {
            _state.update { it.copy(pinError = "Incorrect PIN. Try again.") }
        }
    }

    /**
     * Sets a new parent PIN (hashed for simple local storage).
     */
    private fun setNewPin(pin: String) {
        if (pin.length != PIN_LENGTH) {
            _state.update { it.copy(pinError = "PIN must be $PIN_LENGTH digits") }
            return
        }
        storedPinHash = pin.hashCode()
        _state.update {
            it.copy(
                showParentZone = true,
                showPinDialog = false,
                pinError = null,
                parentPinSet = true,
            )
        }
        viewModelScope.launch {
            _effects.emit(SettingsEffect.ShowToast("Parent PIN set successfully"))
        }
    }

    private fun dismissPinDialog() {
        _state.update { it.copy(showPinDialog = false, pinError = null) }
    }

    private fun requestReset() {
        _state.update { it.copy(showResetDialog = true) }
    }

    /**
     * Confirms the app reset only if the user typed "RESET" exactly.
     * Clears the database by restoring an empty backup and emits [SettingsEffect.AppReset].
     */
    private fun confirmReset(confirmText: String) {
        if (confirmText != RESET_CONFIRMATION_TEXT) {
            viewModelScope.launch {
                _effects.emit(
                    SettingsEffect.ShowToast("Type RESET to confirm")
                )
            }
            return
        }
        viewModelScope.launch {
            try {
                // Restore an empty backup to clear all data
                val emptyBackup = """{"version":1,"profile":null}"""
                backupRepository.restoreBackup(emptyBackup)
                settingsRepository.setOnboardingComplete(false)
                _state.update { it.copy(showResetDialog = false) }
                storedPinHash = null
                _effects.emit(SettingsEffect.AppReset)
            } catch (e: Exception) {
                _effects.emit(
                    SettingsEffect.ShowToast("Reset failed: ${e.message}")
                )
            }
        }
    }

    private fun dismissResetDialog() {
        _state.update { it.copy(showResetDialog = false) }
    }

    private fun navigateTo(route: String) {
        viewModelScope.launch {
            _effects.emit(SettingsEffect.NavigateTo(route))
        }
    }

    companion object {
        const val PIN_LENGTH = 4
        const val RESET_CONFIRMATION_TEXT = "RESET"
    }
}

/**
 * Internal data holder used to combine multiple settings flows into
 * a single emission for atomic state updates.
 */
private data class SettingsData(
    val locale: String,
    val isSoundEnabled: Boolean,
    val isHapticEnabled: Boolean,
    val dailyGoal: Int,
    val isAccentStrict: Boolean,
    val selectedTheme: String,
)
