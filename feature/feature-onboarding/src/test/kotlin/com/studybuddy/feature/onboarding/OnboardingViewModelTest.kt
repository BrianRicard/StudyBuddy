package com.studybuddy.feature.onboarding

import app.cash.turbine.test
import com.studybuddy.core.domain.model.VoicePack
import com.studybuddy.core.domain.model.VoicePackStatus
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.repository.VoicePackRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val avatarRepository: AvatarRepository = mockk(relaxed = true)
    private val rewardsRepository: RewardsRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val voicePackRepository: VoicePackRepository = mockk(relaxed = true)

    private val testVoicePacks = listOf(
        VoicePack("vp_fr", "fr", "French", 0L, VoicePackStatus.NOT_INSTALLED),
        VoicePack("vp_en", "en", "English", 0L, VoicePackStatus.NOT_INSTALLED),
        VoicePack("vp_de", "de", "German", 0L, VoicePackStatus.NOT_INSTALLED),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { voicePackRepository.getVoicePacks() } returns flowOf(testVoicePacks)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OnboardingViewModel(
        profileRepository = profileRepository,
        avatarRepository = avatarRepository,
        rewardsRepository = rewardsRepository,
        settingsRepository = settingsRepository,
        voicePackRepository = voicePackRepository,
    )

    @Test
    fun `initial state has onboarding defaults`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertEquals("", state.name)
        assertEquals("en", state.selectedLocale)
    }

    @Test
    fun `set name updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        assertEquals("Alice", viewModel.state.value.name)
    }

    @Test
    fun `set name clears error`() = runTest {
        val viewModel = createViewModel()

        // Trigger error by trying to complete with empty name
        viewModel.onIntent(OnboardingIntent.Complete)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.nameError)

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()
        assertNull(viewModel.state.value.nameError)
    }

    @Test
    fun `select locale updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SelectLocale("de"))
        advanceUntilIdle()

        assertEquals("de", viewModel.state.value.selectedLocale)
    }

    @Test
    fun `select character updates avatar config`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SelectCharacter("unicorn"))
        advanceUntilIdle()

        assertEquals("unicorn", viewModel.state.value.avatarConfig.bodyId)
    }

    @Test
    fun `select hat updates avatar config`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SelectHat("hat_tophat"))
        advanceUntilIdle()

        assertEquals("hat_tophat", viewModel.state.value.avatarConfig.hatId)
    }

    @Test
    fun `select face updates avatar config`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SelectFace("face_shades"))
        advanceUntilIdle()

        assertEquals("face_shades", viewModel.state.value.avatarConfig.faceId)
    }

    @Test
    fun `complete creates profile and emits NavigateToHome`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        viewModel.onIntent(OnboardingIntent.SelectLocale("fr"))
        viewModel.onIntent(OnboardingIntent.SelectCharacter("unicorn"))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(OnboardingIntent.Complete)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is OnboardingEffect.NavigateToHome)
        }

        coVerify { profileRepository.updateProfile(any()) }
        coVerify { avatarRepository.saveAvatarConfig(any(), any()) }
        coVerify { settingsRepository.setAppLocale("fr") }
        coVerify { settingsRepository.setOnboardingComplete(true) }
    }

    @Test
    fun `complete with empty name shows error instead of completing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.Complete)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.nameError)
        assertFalse(viewModel.state.value.isCompleting)
    }

    @Test
    fun `complete enables all voice packs`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(OnboardingIntent.Complete)
            advanceUntilIdle()
            awaitItem()
        }

        coVerify { voicePackRepository.updateVoicePackStatus("vp_fr", VoicePackStatus.INSTALLED) }
        coVerify { voicePackRepository.updateVoicePackStatus("vp_en", VoicePackStatus.INSTALLED) }
        coVerify { voicePackRepository.updateVoicePackStatus("vp_de", VoicePackStatus.INSTALLED) }
    }

    @Test
    fun `complete skips already installed voice packs`() = runTest {
        val mixedPacks = listOf(
            VoicePack("vp_fr", "fr", "French", 0L, VoicePackStatus.NOT_INSTALLED),
            VoicePack("vp_en", "en", "English", 0L, VoicePackStatus.INSTALLED),
        )
        coEvery { voicePackRepository.getVoicePacks() } returns flowOf(mixedPacks)

        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(OnboardingIntent.Complete)
            advanceUntilIdle()
            awaitItem()
        }

        coVerify { voicePackRepository.updateVoicePackStatus("vp_fr", VoicePackStatus.INSTALLED) }
        coVerify(exactly = 0) { voicePackRepository.updateVoicePackStatus("vp_en", any()) }
    }
}
