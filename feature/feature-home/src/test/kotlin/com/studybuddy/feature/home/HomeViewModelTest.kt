package com.studybuddy.feature.home

import app.cash.turbine.test
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.Profile
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val profileRepository: ProfileRepository = mockk()
    private val avatarRepository: AvatarRepository = mockk()
    private val pointsRepository: PointsRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()

    private val testProfile = Profile(
        id = "test-id",
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
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultMocks(
        profile: Profile = testProfile,
        totalPoints: Long = 100L,
        pointEvents: List<PointEvent> = emptyList(),
        sessionsToday: Int = 0,
        locale: String = "en",
        dailyGoal: Int = 5,
    ) {
        every { profileRepository.getActiveProfile() } returns flowOf(profile)
        every { avatarRepository.getAvatarConfig(profile.id) } returns flowOf(profile.avatarConfig)
        every { pointsRepository.getTotalPoints(profile.id) } returns flowOf(totalPoints)
        every { pointsRepository.getPointsForProfile(profile.id) } returns flowOf(pointEvents)
        every { pointsRepository.getPointsToday(profile.id) } returns flowOf(sessionsToday)
        every { pointsRepository.getSessionsToday(profile.id) } returns flowOf(sessionsToday)
        every { settingsRepository.getAppLocale() } returns flowOf(locale)
        every { settingsRepository.getDailyGoal() } returns flowOf(dailyGoal)
    }

    private fun createViewModel() = HomeViewModel(
        profileRepository = profileRepository,
        avatarRepository = avatarRepository,
        pointsRepository = pointsRepository,
        settingsRepository = settingsRepository,
    )

    @Test
    fun `initial state is loading`() {
        setupDefaultMocks()
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `loads profile data from repositories`() = runTest {
        setupDefaultMocks(totalPoints = 250L)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Test Kid", state.profileName)
        assertEquals(250L, state.totalStars)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loads settings from repository`() = runTest {
        setupDefaultMocks(locale = "fr", dailyGoal = 10)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("fr", state.locale)
        assertEquals(10, state.dailyGoal)
    }

    @Test
    fun `daily progress is calculated correctly`() = runTest {
        setupDefaultMocks(sessionsToday = 3, dailyGoal = 5)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.sessionsToday)
        assertEquals(0.6f, state.dailyProgress, 0.01f)
        assertFalse(state.isDailyGoalReached)
    }

    @Test
    fun `daily goal reached when sessions meet target`() = runTest {
        setupDefaultMocks(sessionsToday = 5, dailyGoal = 5)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isDailyGoalReached)
    }

    @Test
    fun `navigate intent emits correct effect`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.NavigateToDictee)
            assertEquals(HomeEffect.OpenDictee, awaitItem())
        }
    }

    @Test
    fun `navigate to math emits OpenMath effect`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.NavigateToMath)
            assertEquals(HomeEffect.OpenMath, awaitItem())
        }
    }

    @Test
    fun `navigate to avatar emits OpenAvatar effect`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.NavigateToAvatar)
            assertEquals(HomeEffect.OpenAvatar, awaitItem())
        }
    }

    @Test
    fun `buildGreeting returns French greetings for fr locale`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        val greeting = viewModel.buildGreeting("fr")
        assertTrue(
            greeting in listOf("Bonjour", "Bon après-midi", "Bonsoir"),
            "French greeting should be one of the expected values, got: $greeting",
        )
    }

    @Test
    fun `buildGreeting returns English greetings for en locale`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        val greeting = viewModel.buildGreeting("en")
        assertTrue(
            greeting in listOf("Good morning", "Good afternoon", "Good evening"),
            "English greeting should be one of the expected values, got: $greeting",
        )
    }

    @Test
    fun `buildGreeting returns German greetings for de locale`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        val greeting = viewModel.buildGreeting("de")
        assertTrue(
            greeting in listOf("Guten Morgen", "Guten Tag", "Guten Abend"),
            "German greeting should be one of the expected values, got: $greeting",
        )
    }

    @Test
    fun `calculateDayStreak returns 0 for empty events`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        val streak = viewModel.calculateDayStreak(
            emptyList(),
            kotlinx.datetime.TimeZone.currentSystemDefault(),
        )
        assertEquals(0, streak)
    }

    @Test
    fun `recent activities limited to 5 items`() = runTest {
        val now = Clock.System.now()
        val events = (1..10).map { i ->
            PointEvent(
                id = "event_$i",
                profileId = "test-id",
                source = PointSource.MATH,
                points = 10,
                reason = "Problem $i",
                timestamp = now - kotlin.time.Duration.parse("${i}m"),
            )
        }
        setupDefaultMocks(pointEvents = events)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.recentActivities.size <= 5)
    }

    @Test
    fun `week dots has 7 entries`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(7, viewModel.state.value.weekDots.size)
    }

    @Test
    fun `sessionsToday uses session count not points sum`() = runTest {
        // Regression: sessionsToday must reflect the number of sessions (getSessionsToday),
        // not the total points earned today (getPointsToday).
        // After 1 session worth 300 points, sessionsToday should be 1, not 300.
        val profile = testProfile
        every { profileRepository.getActiveProfile() } returns flowOf(profile)
        every { avatarRepository.getAvatarConfig(profile.id) } returns flowOf(profile.avatarConfig)
        every { pointsRepository.getTotalPoints(profile.id) } returns flowOf(300L)
        every { pointsRepository.getPointsForProfile(profile.id) } returns flowOf(
            listOf(
                PointEvent(
                    id = "event_1",
                    profileId = profile.id,
                    source = PointSource.MATH,
                    points = 300,
                    reason = "Math session: 5/5 correct",
                    timestamp = Clock.System.now(),
                ),
            ),
        )
        // getSessionsToday returns 1 (one session), getPointsToday returns 300 (total points)
        every { pointsRepository.getSessionsToday(profile.id) } returns flowOf(1)
        every { pointsRepository.getPointsToday(profile.id) } returns flowOf(300)
        every { settingsRepository.getAppLocale() } returns flowOf("en")
        every { settingsRepository.getDailyGoal() } returns flowOf(5)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        // sessionsToday should be 1 (session count), NOT 300 (points sum)
        assertEquals(1, state.sessionsToday)
        // Verify the ViewModel calls getSessionsToday, not getPointsToday
        verify { pointsRepository.getSessionsToday(profile.id) }
    }
}
