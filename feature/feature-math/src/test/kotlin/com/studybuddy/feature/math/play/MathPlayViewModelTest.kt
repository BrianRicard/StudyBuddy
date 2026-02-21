package com.studybuddy.feature.math.play

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.usecase.math.CheckAnswerUseCase
import com.studybuddy.core.domain.usecase.math.GenerateProblemUseCase
import com.studybuddy.core.domain.usecase.math.SaveMathSessionUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class MathPlayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val generateProblem: GenerateProblemUseCase = mockk()
    private val checkAnswer: CheckAnswerUseCase = mockk()
    private val saveMathSession: SaveMathSessionUseCase = mockk(relaxed = true)
    private val awardPoints: AwardPointsUseCase = mockk(relaxed = true)

    private val testProblem = MathProblem(
        operandA = 7,
        operandB = 3,
        operator = Operator.PLUS,
        correctAnswer = 10,
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every {
            generateProblem(
                operators = any(),
                range = any(),
                difficulty = any(),
                currentStreak = any(),
            )
        } returns testProblem
        every { checkAnswer(any(), eq(10)) } returns Feedback.Correct
        every { checkAnswer(any(), neq(10)) } returns Feedback.Incorrect("10")
        coEvery { awardPoints(any(), any(), any(), any(), any()) } returns 50
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        operators: String = "PLUS",
        rangeMin: Int = 1,
        rangeMax: Int = 12,
        timerSeconds: Int = 15,
        problemCount: Int = 5,
    ): MathPlayViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "operators" to operators,
                "rangeMin" to rangeMin,
                "rangeMax" to rangeMax,
                "timerSeconds" to timerSeconds,
                "problemCount" to problemCount,
            ),
        )
        return MathPlayViewModel(
            generateProblem = generateProblem,
            checkAnswer = checkAnswer,
            saveMathSession = saveMathSession,
            awardPoints = awardPoints,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `initial state loads first problem`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.currentProblem)
        assertEquals(testProblem, state.currentProblem)
        assertEquals("", state.userAnswer)
        assertEquals(0, state.problemsCompleted)
        assertEquals(5, state.totalProblems)
        assertFalse(state.isComplete)
    }

    @Test
    fun `digit entered appends to user answer`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        advanceUntilIdle()

        assertEquals("1", viewModel.state.value.userAnswer)

        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        advanceUntilIdle()

        assertEquals("10", viewModel.state.value.userAnswer)
    }

    @Test
    fun `backspace removes last digit`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(2))
        advanceUntilIdle()
        assertEquals("12", viewModel.state.value.userAnswer)

        viewModel.onIntent(MathPlayIntent.Backspace)
        advanceUntilIdle()

        assertEquals("1", viewModel.state.value.userAnswer)
    }

    @Test
    fun `backspace on empty answer does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Backspace)
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.userAnswer)
    }

    @Test
    fun `submit correct answer increments correct count and streak`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.correctCount)
        assertEquals(1, state.streak)
    }

    @Test
    fun `submit incorrect answer resets streak`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // First answer correctly to build streak
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.streak)

        // Now answer incorrectly
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.streak)
    }

    @Test
    fun `submit empty answer does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Submit)
        advanceUntilIdle()

        // No feedback should be set
        assertNull(viewModel.state.value.feedback)
    }

    @Test
    fun `pause sets isPaused to true`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Pause)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isPaused)
    }

    @Test
    fun `resume sets isPaused to false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Pause)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isPaused)

        viewModel.onIntent(MathPlayIntent.Resume)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPaused)
    }

    @Test
    fun `digit input ignored when paused`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.Pause)
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.userAnswer)
    }

    @Test
    fun `digit input ignored when feedback is shown`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceUntilIdle()

        // Now try to input while feedback is visible
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        advanceUntilIdle()

        // Should still be "10", not "105"
        assertFalse(viewModel.state.value.userAnswer.contains("5"))
    }

    @Test
    fun `completing all problems emits GameComplete effect`() = runTest {
        val viewModel = createViewModel(problemCount = 2)
        advanceUntilIdle()

        viewModel.effects.test {
            // Answer problem 1
            viewModel.onIntent(MathPlayIntent.DigitEntered(1))
            viewModel.onIntent(MathPlayIntent.DigitEntered(0))
            viewModel.onIntent(MathPlayIntent.Submit)
            advanceTimeBy(2_000)
            advanceUntilIdle()

            // Answer problem 2
            viewModel.onIntent(MathPlayIntent.DigitEntered(1))
            viewModel.onIntent(MathPlayIntent.DigitEntered(0))
            viewModel.onIntent(MathPlayIntent.Submit)
            advanceTimeBy(2_000)
            advanceUntilIdle()

            // Collect effects
            val effects = mutableListOf<MathPlayEffect>()
            while (true) {
                val effect = tryReceive().getOrNull() ?: break
                effects.add(effect)
            }

            assertTrue(effects.any { it is MathPlayEffect.GameComplete })
        }
    }

    @Test
    fun `completing session saves math session`() = runTest {
        val viewModel = createViewModel(problemCount = 1)
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        coVerify { saveMathSession(any()) }
    }

    @Test
    fun `completing session awards points`() = runTest {
        val viewModel = createViewModel(problemCount = 1)
        advanceUntilIdle()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        coVerify {
            awardPoints(
                profileId = any(),
                basePoints = any(),
                streak = any(),
                source = PointSource.MATH,
                reason = any(),
            )
        }
    }

    @Test
    fun `best streak tracks maximum streak achieved`() = runTest {
        val viewModel = createViewModel(problemCount = 5)
        advanceUntilIdle()

        // Correct answer
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        // Correct answer
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.bestStreak)

        // Wrong answer - streak resets but bestStreak stays
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(3_000)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.streak)
        assertEquals(2, viewModel.state.value.bestStreak)
    }

    @Test
    fun `multiple operators are parsed from saved state`() = runTest {
        val viewModel = createViewModel(operators = "PLUS,MINUS,MULTIPLY")
        advanceUntilIdle()

        // ViewModel should initialize successfully
        assertNotNull(viewModel.state.value.currentProblem)
    }
}
