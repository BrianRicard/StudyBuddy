package com.studybuddy.feature.onboarding

import app.cash.turbine.test
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
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
    )

    @Test
    fun `initial state starts at step 0`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.state.value.currentStep)
    }

    @Test
    fun `initial state has onboarding defaults`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertEquals(0, state.currentStep)
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

        // Trigger error by trying next with empty name
        viewModel.onIntent(OnboardingIntent.NextStep)
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
    fun `next step with empty name shows error`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.nameError)
        assertEquals(0, viewModel.state.value.currentStep)
    }

    @Test
    fun `next step with valid name advances to step 1`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentStep)
    }

    @Test
    fun `next step from step 1 stays at step 1 (final step)`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentStep)
    }

    @Test
    fun `previous step goes back`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingIntent.PreviousStep)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentStep)
    }

    @Test
    fun `previous step from step 0 stays at 0`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.PreviousStep)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentStep)
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

    // --- Regression tests: nextStep only advances by 1 step (bug fix #16) ---

    @Test
    fun `nextStep from step 0 advances to exactly step 1 not step 2`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()

        assertEquals(
            1,
            viewModel.state.value.currentStep,
            "nextStep() from step 0 must land on step 1, never skip to step 2",
        )
    }

    @Test
    fun `nextStep visits all 2 steps in order 0 then 1`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        // Step 0 -> 1
        assertEquals(0, viewModel.state.value.currentStep)
        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.currentStep)

        // Step 1 is the final step (STEP_VOICE), nextStep should NOT advance further
        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()
        assertEquals(
            1,
            viewModel.state.value.currentStep,
            "nextStep() at final step (1) must not advance beyond it",
        )
    }

    @Test
    fun `nextStep from step 0 caps at step 1`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(OnboardingIntent.SetName("Alice"))
        advanceUntilIdle()

        // Call nextStep twice rapidly — should cap at step 1 (final step)
        viewModel.onIntent(OnboardingIntent.NextStep)
        viewModel.onIntent(OnboardingIntent.NextStep)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentStep)
    }

    @Test
    fun `previousStep at step 0 does not go below 0`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.state.value.currentStep)

        // Try going back multiple times from step 0
        viewModel.onIntent(OnboardingIntent.PreviousStep)
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingIntent.PreviousStep)
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.currentStep)

        // Verify the step is never negative
        assertTrue(
            viewModel.state.value.currentStep >= 0,
            "currentStep must never be negative",
        )
    }

    @Test
    fun `previousStep at step 0 repeated 5 times stays at 0`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.state.value.currentStep)

        repeat(5) {
            viewModel.onIntent(OnboardingIntent.PreviousStep)
            advanceUntilIdle()
        }

        assertEquals(
            0,
            viewModel.state.value.currentStep,
            "previousStep() called repeatedly at step 0 must never go below 0",
        )
    }
}
