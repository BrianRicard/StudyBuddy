package com.studybuddy.feature.math.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

    private var lastViewModel: MathPlayViewModel? = null

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
        lastViewModel?.viewModelScope?.cancel()
        lastViewModel = null
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
            rewardCalculator = com.studybuddy.shared.points.RewardCalculator(),
            savedStateHandle = savedStateHandle,
        ).also { lastViewModel = it }
    }

    @Test
    fun `initial state loads first problem`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        val state = viewModel.state.value
        assertNotNull(state.currentProblem)
        assertEquals(testProblem, state.currentProblem)
        assertEquals("", state.userAnswer)
        assertEquals(0, state.problemsCompleted)
        assertEquals(5, state.totalProblems)
        assertFalse(state.isComplete)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `digit entered appends to user answer`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        assertEquals("1", viewModel.state.value.userAnswer)

        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        assertEquals("10", viewModel.state.value.userAnswer)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `backspace removes last digit`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(2))
        assertEquals("12", viewModel.state.value.userAnswer)

        viewModel.onIntent(MathPlayIntent.Backspace)
        assertEquals("1", viewModel.state.value.userAnswer)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `backspace on empty answer does nothing`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.Backspace)
        assertEquals("", viewModel.state.value.userAnswer)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `submit correct answer increments correct count and streak`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        runCurrent()

        val state = viewModel.state.value
        assertEquals(1, state.correctCount)
        assertEquals(1, state.streak)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `submit incorrect answer resets streak`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        // First answer correctly to build streak
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(1, viewModel.state.value.streak)

        // Now answer incorrectly
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        viewModel.onIntent(MathPlayIntent.Submit)
        runCurrent()

        assertEquals(0, viewModel.state.value.streak)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `submit empty answer does nothing`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.Submit)
        runCurrent()

        // No feedback should be set
        assertNull(viewModel.state.value.feedback)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `pause sets isPaused to true`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.Pause)
        assertTrue(viewModel.state.value.isPaused)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `resume sets isPaused to false`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.Pause)
        assertTrue(viewModel.state.value.isPaused)

        viewModel.onIntent(MathPlayIntent.Resume)
        assertFalse(viewModel.state.value.isPaused)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `digit input ignored when paused`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.Pause)
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        assertEquals("", viewModel.state.value.userAnswer)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `digit input ignored when feedback is shown`() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        runCurrent()

        // Now try to input while feedback is visible
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))

        // Should still be "10", not "105"
        assertFalse(viewModel.state.value.userAnswer.contains("5"))

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `completing all problems emits GameComplete effect`() = runTest {
        val viewModel = createViewModel(problemCount = 2)
        runCurrent()

        viewModel.effects.test {
            // Answer problem 1
            viewModel.onIntent(MathPlayIntent.DigitEntered(1))
            viewModel.onIntent(MathPlayIntent.DigitEntered(0))
            viewModel.onIntent(MathPlayIntent.Submit)
            advanceTimeBy(2_000)
            runCurrent()

            // Answer problem 2
            viewModel.onIntent(MathPlayIntent.DigitEntered(1))
            viewModel.onIntent(MathPlayIntent.DigitEntered(0))
            viewModel.onIntent(MathPlayIntent.Submit)
            advanceTimeBy(2_000)
            runCurrent()

            // Collect effects
            val effects = cancelAndConsumeRemainingEvents()
            assertTrue(
                effects.filterIsInstance<app.cash.turbine.Event.Item<MathPlayEffect>>().any {
                    it.value is MathPlayEffect.GameComplete
                },
            )
        }

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `completing session saves math session`() = runTest {
        val viewModel = createViewModel(problemCount = 1)
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        coVerify { saveMathSession(any()) }

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `completing session awards points`() = runTest {
        val viewModel = createViewModel(problemCount = 1)
        runCurrent()

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        coVerify {
            awardPoints(
                profileId = any(),
                basePoints = any(),
                streak = any(),
                source = PointSource.MATH,
                reason = any(),
            )
        }

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `best streak tracks maximum streak achieved`() = runTest {
        val viewModel = createViewModel(problemCount = 5)
        runCurrent()

        // Correct answer
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        // Correct answer
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(2, viewModel.state.value.bestStreak)

        // Wrong answer - streak resets but bestStreak stays
        viewModel.onIntent(MathPlayIntent.DigitEntered(5))
        viewModel.onIntent(MathPlayIntent.Submit)
        runCurrent()

        assertEquals(0, viewModel.state.value.streak)
        assertEquals(2, viewModel.state.value.bestStreak)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `multiple operators are parsed from saved state`() = runTest {
        val viewModel = createViewModel(operators = "PLUS,MINUS,MULTIPLY")
        runCurrent()

        // ViewModel should initialize successfully
        assertNotNull(viewModel.state.value.currentProblem)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `completeSession awards points with streak 0 to avoid double multiplier`() = runTest {
        // Regression: completeSession must pass streak=0 to awardPoints because
        // PointsCalculator.calculateMathPoints already includes streak bonuses.
        // Passing the actual streak would apply the multiplier twice, inflating points.
        val viewModel = createViewModel(problemCount = 2)
        runCurrent()

        // Answer problem 1 correctly
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        // Answer problem 2 correctly (streak = 2)
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        // Verify session is complete
        assertTrue(viewModel.state.value.isComplete)

        // Verify awardPoints was called with streak = 0 (not the actual streak of 2)
        // to prevent double-applying the streak multiplier
        coVerify {
            awardPoints(
                profileId = any(),
                basePoints = any(),
                streak = eq(0),
                source = eq(PointSource.MATH),
                reason = any(),
            )
        }

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `completeSession awards points via RewardCalculator`() = runTest {
        // RewardCalculator computes session points based on difficulty factors.
        // With 2 correct out of 2, PLUS only, range 1-12, timer 15s:
        //   base=2, timeFactor=1.8, opFactor=0.8, rangeFactor=1.0
        //   diffMult=1.44, accMult=1.5, raw=floor(2*1.44*1.5)=4, volumeBonus=0, total=4
        val expectedScore = 4
        coEvery {
            awardPoints(
                profileId = any(),
                basePoints = eq(expectedScore),
                streak = eq(0),
                source = eq(PointSource.MATH),
                reason = any(),
            )
        } returns expectedScore

        val viewModel = createViewModel(problemCount = 2)
        runCurrent()

        // Answer problem 1 correctly
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        // Answer problem 2 correctly
        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(0))
        viewModel.onIntent(MathPlayIntent.Submit)
        advanceTimeBy(2_000)
        runCurrent()

        val state = viewModel.state.value
        assertTrue(state.isComplete)
        assertEquals(expectedScore, state.pointsAwarded)

        // Verify the exact call: streak must be 0 to avoid double multiplier
        coVerify {
            awardPoints(
                profileId = any(),
                basePoints = eq(expectedScore),
                streak = eq(0),
                source = eq(PointSource.MATH),
                reason = eq("Math session: 2/2 correct"),
            )
        }

        viewModel.viewModelScope.cancel()
    }
}
