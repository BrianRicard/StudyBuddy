package com.studybuddy.feature.math.results

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MathResultsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        totalProblems: Int = 20,
        correctCount: Int = 15,
        bestStreak: Int = 8,
        avgResponseMs: Long = 4500L,
        sessionScore: Int = 75,
    ): MathResultsViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "totalProblems" to totalProblems,
                "correctCount" to correctCount,
                "bestStreak" to bestStreak,
                "avgResponseMs" to avgResponseMs,
                "sessionScore" to sessionScore,
            ),
        )
        return MathResultsViewModel(
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `initial state calculates accuracy correctly`() = runTest {
        val viewModel = createViewModel(totalProblems = 20, correctCount = 15)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0.75f, state.accuracy)
    }

    @Test
    fun `initial state shows correct totals`() = runTest {
        val viewModel = createViewModel(
            totalProblems = 20,
            correctCount = 15,
            bestStreak = 8,
            avgResponseMs = 4500L,
            sessionScore = 75,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(20, state.totalProblems)
        assertEquals(15, state.correctCount)
        assertEquals(8, state.bestStreak)
        assertEquals(4500L, state.avgResponseMs)
        assertEquals(75, state.sessionScore)
    }

    @Test
    fun `streak master badge awarded for streak of 10 or more`() = runTest {
        val viewModel = createViewModel(bestStreak = 12)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.badges.contains(MathResultsViewModel.BADGE_STREAK_MASTER))
    }

    @Test
    fun `streak master badge not awarded for streak below 10`() = runTest {
        val viewModel = createViewModel(bestStreak = 5)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.badges.none { it == MathResultsViewModel.BADGE_STREAK_MASTER })
    }

    @Test
    fun `speed demon badge awarded for fast avg response time`() = runTest {
        val viewModel = createViewModel(avgResponseMs = 2500L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.badges.contains(MathResultsViewModel.BADGE_SPEED_DEMON))
    }

    @Test
    fun `speed demon badge not awarded for slow response time`() = runTest {
        val viewModel = createViewModel(avgResponseMs = 5000L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.badges.none { it == MathResultsViewModel.BADGE_SPEED_DEMON })
    }

    @Test
    fun `navigate home emits NavigateToHome effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathResultsIntent.NavigateHome)
        advanceUntilIdle()

        assertEquals(MathResultsEffect.NavigateToHome, viewModel.effect.value)
    }

    @Test
    fun `play again emits NavigateToSetup effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathResultsIntent.PlayAgain)
        advanceUntilIdle()

        assertEquals(MathResultsEffect.NavigateToSetup, viewModel.effect.value)
    }

    @Test
    fun `consume effect clears effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathResultsIntent.NavigateHome)
        advanceUntilIdle()
        assertEquals(MathResultsEffect.NavigateToHome, viewModel.effect.value)

        viewModel.consumeEffect()
        advanceUntilIdle()

        assertEquals(null, viewModel.effect.value)
    }

    @Test
    fun `zero total problems results in zero accuracy`() = runTest {
        val viewModel = createViewModel(totalProblems = 0, correctCount = 0)
        advanceUntilIdle()

        assertEquals(0f, viewModel.state.value.accuracy)
    }

    @Test
    fun `total points includes session score and streak bonus`() = runTest {
        val viewModel = createViewModel(sessionScore = 75, bestStreak = 8)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(state.sessionScore + state.streakBonus, state.totalPoints)
    }
}
