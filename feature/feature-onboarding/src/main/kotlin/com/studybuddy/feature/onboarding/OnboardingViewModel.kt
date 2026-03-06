package com.studybuddy.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.Profile
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.VoicePackStatus
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.repository.VoicePackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * UI state for the single-step onboarding flow.
 *
 * @property name The name entered by the child.
 * @property selectedLocale The chosen app language code ("fr", "en", or "de").
 * @property avatarConfig The avatar configuration (uses defaults).
 * @property isCompleting True while the final save sequence is running.
 * @property nameError Validation message shown when the name field is empty.
 */
data class OnboardingState(
    val name: String = "",
    val selectedLocale: String = "en",
    val avatarConfig: AvatarConfig = AvatarConfig.default(),
    val isCompleting: Boolean = false,
    val nameError: String? = null,
)

/**
 * User actions dispatched to the Onboarding ViewModel.
 */
sealed interface OnboardingIntent {
    data class SetName(val name: String) : OnboardingIntent
    data class SelectLocale(val locale: String) : OnboardingIntent
    data class SelectCharacter(val bodyId: String) : OnboardingIntent
    data class SelectHat(val hatId: String) : OnboardingIntent
    data class SelectFace(val faceId: String) : OnboardingIntent
    data object Complete : OnboardingIntent
}

/**
 * One-shot side effects emitted by the Onboarding ViewModel.
 */
sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowError(val message: String) : OnboardingEffect
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val avatarRepository: AvatarRepository,
    private val rewardsRepository: RewardsRepository,
    private val settingsRepository: SettingsRepository,
    private val voicePackRepository: VoicePackRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<OnboardingEffect>()
    val effects: SharedFlow<OnboardingEffect> = _effects.asSharedFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.SetName -> setName(intent.name)
            is OnboardingIntent.SelectLocale -> selectLocale(intent.locale)
            is OnboardingIntent.SelectCharacter -> selectCharacter(intent.bodyId)
            is OnboardingIntent.SelectHat -> selectHat(intent.hatId)
            is OnboardingIntent.SelectFace -> selectFace(intent.faceId)
            is OnboardingIntent.Complete -> completeOnboarding()
        }
    }

    private fun setName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null,
            )
        }
    }

    private fun selectLocale(locale: String) {
        _state.update { it.copy(selectedLocale = locale) }
    }

    private fun selectCharacter(bodyId: String) {
        _state.update {
            it.copy(avatarConfig = it.avatarConfig.copy(bodyId = bodyId))
        }
    }

    private fun selectHat(hatId: String) {
        _state.update {
            it.copy(avatarConfig = it.avatarConfig.copy(hatId = hatId))
        }
    }

    private fun selectFace(faceId: String) {
        _state.update {
            it.copy(avatarConfig = it.avatarConfig.copy(faceId = faceId))
        }
    }

    /**
     * Validates input and persists all onboarding data:
     * 1. Creates the user profile in Room.
     * 2. Saves the chosen avatar configuration.
     * 3. Grants all starter items as owned rewards.
     * 4. Enables all voice packs by default.
     * 5. Marks onboarding as complete.
     * 6. Sets the app locale (may trigger Activity recreation).
     * 7. Emits [OnboardingEffect.NavigateToHome].
     */
    private fun completeOnboarding() {
        val current = _state.value

        if (current.name.isBlank()) {
            _state.update { it.copy(nameError = "Please enter your name") }
            return
        }

        if (current.isCompleting) return

        _state.update { it.copy(isCompleting = true) }

        viewModelScope.launch {
            try {
                val profileId = AppConstants.DEFAULT_PROFILE_ID
                val now = Clock.System.now()

                val profile = Profile(
                    id = profileId,
                    name = current.name.trim(),
                    avatarConfig = current.avatarConfig,
                    locale = current.selectedLocale,
                    totalPoints = 0,
                    createdAt = now,
                    updatedAt = now,
                )

                profileRepository.updateProfile(profile)

                avatarRepository.saveAvatarConfig(
                    profileId = profileId,
                    config = current.avatarConfig,
                )

                grantStarterItems(profileId)
                enableAllVoicePacks()

                // Mark onboarding complete BEFORE setting locale, because
                // setAppLocale triggers a locale Flow emission that causes
                // MainActivity to call AppCompatDelegate.setApplicationLocales(),
                // which recreates the Activity. The onboarding flag must be
                // persisted first so the recreated Activity navigates to HOME.
                settingsRepository.setOnboardingComplete(complete = true)
                settingsRepository.setAppLocale(current.selectedLocale)

                _effects.emit(OnboardingEffect.NavigateToHome)
            } catch (e: Exception) {
                _state.update { it.copy(isCompleting = false) }
                _effects.emit(
                    OnboardingEffect.ShowError(
                        e.message ?: "Something went wrong. Please try again.",
                    ),
                )
            }
        }
    }

    /**
     * Marks every starter item from [RewardCatalog.starterItemIds] as owned
     * by the new profile so the child begins with free cosmetics.
     */
    private suspend fun grantStarterItems(profileId: String) {
        RewardCatalog.starterItemIds.forEach { itemId ->
            val item = RewardCatalog.getItemById(itemId) ?: return@forEach
            rewardsRepository.purchaseReward(
                profileId = profileId,
                reward = item,
            )
        }
    }

    /**
     * Enables all available voice packs by setting their status to INSTALLED.
     * This runs during onboarding for new installs only — existing users who
     * update keep their current voice pack preferences.
     */
    private suspend fun enableAllVoicePacks() {
        val packs = voicePackRepository.getVoicePacks().first()
        packs.forEach { pack ->
            if (pack.status != VoicePackStatus.INSTALLED) {
                voicePackRepository.updateVoicePackStatus(
                    id = pack.id,
                    status = VoicePackStatus.INSTALLED,
                )
            }
        }
    }
}
