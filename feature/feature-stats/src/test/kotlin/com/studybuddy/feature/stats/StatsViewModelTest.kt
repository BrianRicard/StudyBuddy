package com.studybuddy.feature.stats

import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.MathRepository
import com.studybuddy.core.domain.repository.PointsRepository
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val pointsRepository: PointsRepository = mockk()
    private val mathRepository: MathRepository = mockk()
    private val dicteeRepository: DicteeRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultMocks(
        totalPoints: Long = 0L,
        pointEvents: List<PointEvent> = emptyList(),
        mathSessions: List<MathSession> = emptyList(),
        dicteeLists: List<DicteeList> = emptyList(),
    ) {
        every { pointsRepository.getTotalPoints("default") } returns flowOf(totalPoints)
        every { pointsRepository.getPointsForProfile("default") } returns flowOf(pointEvents)
        every { mathRepository.getSessionsForProfile("default") } returns flowOf(mathSessions)
        every { dicteeRepository.getListsForProfile("default") } returns flowOf(dicteeLists)
    }

    private fun createViewModel() =
        StatsViewModel(
            pointsRepository = pointsRepository,
            mathRepository = mathRepository,
            dicteeRepository = dicteeRepository,
        )

    private fun createPointEvent(
        source: PointSource = PointSource.MATH,
        points: Int = 10,
        timestamp: Instant = Clock.System.now(),
    ) = PointEvent(
            id = "event_${System.nanoTime()}",
            profileId = "default",
            source = source,
            points = points,
            reason = "test",
            timestamp = timestamp,
        )

    private fun createMathSession(
        avgResponseMs: Long = 3000L,
        completedAt: Instant = Clock.System.now(),
    ) = MathSession(
            id = "session_${System.nanoTime()}",
            profileId = "default",
            operators = setOf(Operator.PLUS),
            numberRange = 1..10,
            totalProblems = 10,
            correctCount = 8,
            bestStreak = 5,
            avgResponseMs = avgResponseMs,
            difficulty = Difficulty.EASY,
            completedAt = completedAt,
        )

    private fun createDicteeList(
        wordCount: Int = 10,
        masteredCount: Int = 5,
        updatedAt: Instant = Clock.System.now(),
    ) = DicteeList(
            id = "list_${System.nanoTime()}",
            profileId = "default",
            title = "Test List",
            language = "fr",
            wordCount = wordCount,
            masteredCount = masteredCount,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = updatedAt,
        )

    @Test
    fun `initial state is loading`() {
        setupDefaultMocks()
        val viewModel = createViewModel()
        // Before advancing, the combine hasn't emitted yet
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `observes total stars from repository`() =
        runTest {
            setupDefaultMocks(totalPoints = 250L)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(250L, viewModel.state.value.totalStars)
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `counts total sessions from point events`() =
        runTest {
            val events = listOf(
                createPointEvent(source = PointSource.DICTEE),
                createPointEvent(source = PointSource.MATH),
                createPointEvent(source = PointSource.MATH),
                createPointEvent(source = PointSource.DAILY_LOGIN),
            )
            setupDefaultMocks(pointEvents = events)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(3, viewModel.state.value.totalSessions)
        }

    @Test
    fun `weekly data has 7 days`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(7, viewModel.state.value.weeklyData.size)
        }

    @Test
    fun `weekly data marks today correctly`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            advanceUntilIdle()

            val todayEntries = viewModel.state.value.weeklyData.filter { it.isToday }
            assertEquals(1, todayEntries.size)
        }

    @Test
    fun `dictee accuracy is null with no lists`() =
        runTest {
            setupDefaultMocks(dicteeLists = emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.state.value.dicteeAccuracy)
        }

    @Test
    fun `dictee accuracy calculated correctly`() =
        runTest {
            val lists = listOf(
                createDicteeList(wordCount = 10, masteredCount = 8),
                createDicteeList(wordCount = 10, masteredCount = 6),
            )
            setupDefaultMocks(dicteeLists = lists)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // (8 + 6) / (10 + 10) = 14/20 = 0.7
            assertEquals(0.7f, viewModel.state.value.dicteeAccuracy)
        }

    @Test
    fun `math avg speed is null with no sessions`() =
        runTest {
            setupDefaultMocks(mathSessions = emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.state.value.mathAvgSpeed)
        }

    @Test
    fun `math avg speed calculated from recent sessions`() =
        runTest {
            val sessions = listOf(
                createMathSession(avgResponseMs = 2000L),
                createMathSession(avgResponseMs = 4000L),
            )
            setupDefaultMocks(mathSessions = sessions)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // (2000 + 4000) / 2 = 3000
            assertEquals(3000L, viewModel.state.value.mathAvgSpeed)
        }

    @Test
    fun `math speed trend is null with insufficient sessions`() =
        runTest {
            val sessions = listOf(
                createMathSession(avgResponseMs = 3000L),
            )
            setupDefaultMocks(mathSessions = sessions)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.state.value.mathSpeedTrend)
        }

    @Test
    fun `dictee accuracy trend is null with fewer than 2 lists`() =
        runTest {
            val lists = listOf(
                createDicteeList(wordCount = 10, masteredCount = 8),
            )
            setupDefaultMocks(dicteeLists = lists)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.state.value.dicteeAccuracyTrend)
        }

    @Test
    fun `calculateDayStreak returns 0 for empty events`() {
        setupDefaultMocks()
        val viewModel = createViewModel()
        assertEquals(0, viewModel.calculateDayStreak(emptyList()))
    }

    @Test
    fun `buildWeeklyData sums points per day`() {
        setupDefaultMocks()
        val viewModel = createViewModel()

        val now = Clock.System.now()
        val events = listOf(
            createPointEvent(points = 10, timestamp = now),
            createPointEvent(points = 20, timestamp = now),
        )

        val weeklyData = viewModel.buildWeeklyData(events)
        val today = weeklyData.first { it.isToday }
        assertEquals(30, today.points)
    }
}
