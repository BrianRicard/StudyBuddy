package com.studybuddy.feature.settings

import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.Profile
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.BackupRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.whisper.ModelDownloadManager
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
    private val awardPointsUseCase: AwardPointsUseCase = mockk(relaxed = true)
    private val pointsRepository: PointsRepository = mockk()
    private val modelDownloadManager: ModelDownloadManager = mockk(relaxed = true)

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
        every { settingsRepository.getDailyGoal() } returns flowOf(5)
        every { settingsRepository.isAccentStrict() } returns flowOf(false)
        every { settingsRepository.getSelectedTheme() } returns flowOf("sunset")
        every { settingsRepository.getParentPinHash() } returns flowOf(null)
        every { settingsRepository.getWhisperModel() } returns flowOf("")
        every { pointsRepository.getTotalPoints(any()) } returns flowOf(500L)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        settingsRepository = settingsRepository,
        profileRepository = profileRepository,
        avatarRepository = avatarRepository,
        backupRepository = backupRepository,
        awardPointsUseCase = awardPointsUseCase,
        pointsRepository = pointsRepository,
        modelDownloadManager = modelDownloadManager,
    )

    @Test
    fun `init loads settings state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("en", state.locale)
        assertTrue(state.isSoundEnabled)
        assertEquals(5, state.dailyGoal)
        assertFalse(state.isAccentStrict)
    }

    @Test
    fun `set locale calls repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.SetLocale("fr"))
        advanceUntilIdle()

        coVerify { settingsRepository.setAppLocale("fr") }
    }

    @Test
    fun `toggle sound calls repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ToggleSound)
        advanceUntilIdle()

        coVerify { settingsRepository.setSoundEnabled(false) }
    }

    @Test
    fun `set daily goal calls repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.SetDailyGoal(10))
        advanceUntilIdle()

        coVerify { settingsRepository.setDailyGoal(10) }
    }

    @Test
    fun `toggle accent strict calls repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ToggleAccentStrict)
        advanceUntilIdle()

        coVerify { settingsRepository.setAccentStrict(true) }
    }

    @Test
    fun `open parent zone shows pin dialog when pin not set`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenParentZone)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showPinDialog)
    }

    @Test
    fun `dismiss pin dialog clears dialog state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenParentZone)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.DismissPinDialog)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showPinDialog)
    }

    @Test
    fun `request reset shows reset dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.RequestReset)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showResetDialog)
    }

    @Test
    fun `dismiss reset dialog clears state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.RequestReset)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.DismissResetDialog)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showResetDialog)
    }

    // --- Gift Points tests ---

    @Test
    fun `open gift points shows dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showGiftPointsDialog)
    }

    @Test
    fun `dismiss gift points dialog clears state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.DismissGiftPointsDialog)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showGiftPointsDialog)
    }

    @Test
    fun `confirm gift points awards points and closes dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ConfirmGiftPoints(100))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showGiftPointsDialog)
        coVerify {
            awardPointsUseCase(
                profileId = any(),
                basePoints = 100,
                streak = 0,
                source = PointSource.GIFT,
                reason = "Gift from parent",
            )
        }
    }

    @Test
    fun `confirm gift points rejects zero amount`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ConfirmGiftPoints(0))
        advanceUntilIdle()

        // Dialog should still be open since 0 is invalid
        assertTrue(viewModel.state.value.showGiftPointsDialog)
    }

    @Test
    fun `confirm gift points rejects negative amount`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ConfirmGiftPoints(-50))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showGiftPointsDialog)
    }

    @Test
    fun `confirm gift points rejects amount exceeding max`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OpenGiftPoints)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ConfirmGiftPoints(100_000))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showGiftPointsDialog)
    }

    @Test
    fun `point balance is observed from repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(500L, viewModel.state.value.currentPointBalance)
    }
}
