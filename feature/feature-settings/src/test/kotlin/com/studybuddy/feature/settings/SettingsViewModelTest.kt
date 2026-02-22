package com.studybuddy.feature.settings

import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.Profile
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.BackupRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val profileRepository: ProfileRepository = mockk()
    private val avatarRepository: AvatarRepository = mockk()
    private val backupRepository: BackupRepository = mockk(relaxed = true)

    private val testProfile = Profile(
        id = "default",
        name = "Test Kid",
        avatarConfig = AvatarConfig.default(),
        locale = "en",
        totalPoints = 100,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { profileRepository.getActiveProfile() } returns flowOf(testProfile)
        every { avatarRepository.getAvatarConfig(any()) } returns flowOf(AvatarConfig.default())
        every { settingsRepository.getAppLocale() } returns flowOf("en")
        every { settingsRepository.isSoundEnabled() } returns flowOf(true)
        every { settingsRepository.isHapticEnabled() } returns flowOf(true)
        every { settingsRepository.getDailyGoal() } returns flowOf(5)
        every { settingsRepository.isAccentStrict() } returns flowOf(false)
        every { settingsRepository.getSelectedTheme() } returns flowOf("sunset")
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SettingsViewModel(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            avatarRepository = avatarRepository,
            backupRepository = backupRepository,
        )

    @Test
    fun `init loads settings state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("en", state.locale)
            assertTrue(state.isSoundEnabled)
            assertTrue(state.isHapticEnabled)
            assertEquals(5, state.dailyGoal)
            assertFalse(state.isAccentStrict)
        }

    @Test
    fun `set locale calls repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.SetLocale("fr"))
            advanceUntilIdle()

            coVerify { settingsRepository.setAppLocale("fr") }
        }

    @Test
    fun `toggle sound calls repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.ToggleSound)
            advanceUntilIdle()

            coVerify { settingsRepository.setSoundEnabled(false) }
        }

    @Test
    fun `toggle haptic calls repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.ToggleHaptic)
            advanceUntilIdle()

            coVerify { settingsRepository.setHapticEnabled(false) }
        }

    @Test
    fun `set daily goal calls repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.SetDailyGoal(10))
            advanceUntilIdle()

            coVerify { settingsRepository.setDailyGoal(10) }
        }

    @Test
    fun `toggle accent strict calls repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.ToggleAccentStrict)
            advanceUntilIdle()

            coVerify { settingsRepository.setAccentStrict(true) }
        }

    @Test
    fun `open parent zone shows pin dialog when pin not set`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.OpenParentZone)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.showPinDialog)
        }

    @Test
    fun `dismiss pin dialog clears dialog state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.OpenParentZone)
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.DismissPinDialog)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.showPinDialog)
        }

    @Test
    fun `request reset shows reset dialog`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.RequestReset)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.showResetDialog)
        }

    @Test
    fun `dismiss reset dialog clears state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.RequestReset)
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.DismissResetDialog)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.showResetDialog)
        }
}
