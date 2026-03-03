package com.studybuddy.feature.math.challenge

import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.usecase.math.GenerateProblemUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MathChallengeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val generateProblem: GenerateProblemUseCase = mockk()
    private val awardPoints: AwardPointsUseCase = mockk()

    private fun createProblem(
        a: Int = 3,
        b: Int = 5,
        answer: Int = 8,
    ): MathProblem = MathProblem(a, b, Operator.PLUS, answer)

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
        } returns createProblem()
        coEvery {
            awardPoints(
                profileId = any(),
                basePoints = any(),
                streak = any(),
                source = any(),
                reason = any(),
            )
        } returns 0
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MathChallengeViewModel = MathChallengeViewModel(
        generateProblem,
        awardPoints,
        com.studybuddy.shared.points.RewardCalculator(),
    )

    /** Time needed for the first equation to spawn (initial delay + spawn interval ticks). */
    private val spawnTimeMs: Long
        get() = MathChallengeViewModel.INITIAL_DELAY_MS +
            MathChallengeViewModel.BASE_SPAWN_INTERVAL * MathChallengeViewModel.TICK_MS +
            MathChallengeViewModel.TICK_MS // extra tick buffer

    @Test
    fun `initial state has correct defaults`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val state = vm.state.value

        assertEquals(MathChallengeState.INITIAL_LIVES, state.lives)
        assertEquals(MathChallengeState.INITIAL_BOMBS, state.bombs)
        assertEquals(0, state.score)
        assertEquals(0, state.streak)
        assertEquals(1, state.level)
        assertEquals(0, state.equationsSolved)
        assertFalse(state.isGameOver)
        assertFalse(state.isPaused)
        assertEquals("", state.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `digit entry appends to answer`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onIntent(MathChallengeIntent.DigitEntered(8))
        assertEquals("8", vm.state.value.userAnswer)

        vm.onIntent(MathChallengeIntent.DigitEntered(3))
        assertEquals("83", vm.state.value.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `backspace removes last digit`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onIntent(MathChallengeIntent.DigitEntered(1))
        vm.onIntent(MathChallengeIntent.DigitEntered(2))
        vm.onIntent(MathChallengeIntent.Backspace)

        assertEquals("1", vm.state.value.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `backspace on empty answer does nothing`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onIntent(MathChallengeIntent.Backspace)
        assertEquals("", vm.state.value.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `pause and resume toggle state`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onIntent(MathChallengeIntent.Pause)
        assertTrue(vm.state.value.isPaused)

        vm.onIntent(MathChallengeIntent.Resume)
        assertFalse(vm.state.value.isPaused)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `digit entry ignored when paused`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onIntent(MathChallengeIntent.Pause)
        vm.onIntent(MathChallengeIntent.DigitEntered(5))

        assertEquals("", vm.state.value.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `digit entry respects max length`() = runTest(testDispatcher) {
        val vm = createViewModel()

        repeat(MathChallengeViewModel.MAX_ANSWER_LENGTH + 2) { i ->
            vm.onIntent(MathChallengeIntent.DigitEntered(i % 10))
        }

        assertEquals(
            MathChallengeViewModel.MAX_ANSWER_LENGTH,
            vm.state.value.userAnswer.length,
        )

        vm.viewModelScope.cancel()
    }

    @Test
    fun `equations spawn after initial delay`() = runTest(testDispatcher) {
        val vm = createViewModel()

        // Before initial delay
        assertEquals(0, vm.state.value.equations.size)

        // Advance past initial delay + spawn interval
        advanceTimeBy(spawnTimeMs)

        assertTrue(vm.state.value.equations.isNotEmpty())

        vm.viewModelScope.cancel()
    }

    @Test
    fun `submitting correct answer removes equation and increments score`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())
        val initialCount = vm.state.value.equations.size

        // Type the correct answer (8 = 3 + 5)
        vm.onIntent(MathChallengeIntent.DigitEntered(8))
        vm.onIntent(MathChallengeIntent.Submit)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)

        assertTrue(vm.state.value.equations.size < initialCount)
        assertTrue(vm.state.value.score > 0)
        assertEquals(1, vm.state.value.equationsSolved)
        assertEquals(1, vm.state.value.streak)
        assertEquals("", vm.state.value.userAnswer)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `submitting wrong answer clears input but does not remove equation`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        val countBefore = vm.state.value.equations.size
        assertTrue(countBefore > 0)

        vm.onIntent(MathChallengeIntent.DigitEntered(9))
        vm.onIntent(MathChallengeIntent.DigitEntered(9))
        vm.onIntent(MathChallengeIntent.Submit)

        assertEquals("", vm.state.value.userAnswer)
        assertEquals(0, vm.state.value.score)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `wrong answer breaks streak`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        // Solve one correctly to build streak
        vm.onIntent(MathChallengeIntent.DigitEntered(8))
        vm.onIntent(MathChallengeIntent.Submit)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)
        assertEquals(1, vm.state.value.streak)

        // Wait for another equation
        advanceTimeBy(respawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        // Wrong answer breaks streak
        vm.onIntent(MathChallengeIntent.DigitEntered(9))
        vm.onIntent(MathChallengeIntent.DigitEntered(9))
        vm.onIntent(MathChallengeIntent.Submit)

        assertEquals(0, vm.state.value.streak)
        assertEquals(0, vm.state.value.consecutiveCorrect)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `bomb clears all equations`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())
        assertEquals(MathChallengeState.INITIAL_BOMBS, vm.state.value.bombs)

        vm.onIntent(MathChallengeIntent.UseBomb)
        advanceTimeBy(MathChallengeViewModel.BOMB_FLASH_MS + MathChallengeViewModel.TICK_MS)

        assertEquals(0, vm.state.value.equations.size)
        assertEquals(MathChallengeState.INITIAL_BOMBS - 1, vm.state.value.bombs)

        vm.viewModelScope.cancel()
    }

    /** Time needed for equations to respawn after being cleared (full spawn interval). */
    private val respawnTimeMs: Long
        get() = MathChallengeViewModel.BASE_SPAWN_INTERVAL * MathChallengeViewModel.TICK_MS +
            MathChallengeViewModel.TICK_MS

    @Test
    fun `bomb does nothing when no bombs left`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        // Use bomb #1
        vm.onIntent(MathChallengeIntent.UseBomb)
        advanceTimeBy(MathChallengeViewModel.BOMB_FLASH_MS + MathChallengeViewModel.TICK_MS)
        assertEquals(1, vm.state.value.bombs)

        // Wait for equations to respawn before using bomb #2
        advanceTimeBy(respawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        // Use bomb #2
        vm.onIntent(MathChallengeIntent.UseBomb)
        advanceTimeBy(MathChallengeViewModel.BOMB_FLASH_MS + MathChallengeViewModel.TICK_MS)
        assertEquals(0, vm.state.value.bombs)

        // Wait for more equations to appear
        advanceTimeBy(respawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        val countBefore = vm.state.value.equations.size
        vm.onIntent(MathChallengeIntent.UseBomb)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)

        // No bomb used, equations untouched
        assertEquals(0, vm.state.value.bombs)
        assertEquals(countBefore, vm.state.value.equations.size)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `multiplier scales with streak`() = runTest(testDispatcher) {
        assertEquals(1.0f, MathChallengeState(streak = 0).multiplier)
        assertEquals(1.0f, MathChallengeState(streak = 4).multiplier)
        assertEquals(1.5f, MathChallengeState(streak = 5).multiplier)
        assertEquals(2.0f, MathChallengeState(streak = 10).multiplier)
        assertEquals(3.0f, MathChallengeState(streak = 20).multiplier)
    }

    @Test
    fun `difficulty scaling - operators increase with level`() = runTest(testDispatcher) {
        val vm = createViewModel()

        val ops1 = vm.operatorsForLevel(1)
        assertEquals(setOf(Operator.PLUS, Operator.MINUS), ops1)

        val ops3 = vm.operatorsForLevel(3)
        assertTrue(Operator.MULTIPLY in ops3)
        assertFalse(Operator.DIVIDE in ops3)

        val ops5 = vm.operatorsForLevel(5)
        assertTrue(Operator.DIVIDE in ops5)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `difficulty scaling - range increases with level`() = runTest(testDispatcher) {
        val vm = createViewModel()

        assertEquals(1..10, vm.rangeForLevel(1))
        assertEquals(1..20, vm.rangeForLevel(3))
        assertEquals(1..50, vm.rangeForLevel(10)) // Capped at 50

        vm.viewModelScope.cancel()
    }

    @Test
    fun `difficulty scaling - fall speed increases with level`() = runTest(testDispatcher) {
        val vm = createViewModel()

        assertTrue(vm.fallSpeedForLevel(5) > vm.fallSpeedForLevel(1))

        vm.viewModelScope.cancel()
    }

    @Test
    fun `difficulty scaling - spawn interval decreases with level`() = runTest(testDispatcher) {
        val vm = createViewModel()

        assertTrue(vm.spawnIntervalForLevel(5) < vm.spawnIntervalForLevel(1))
        assertTrue(
            vm.spawnIntervalForLevel(100) >= MathChallengeViewModel.MIN_SPAWN_INTERVAL,
        )

        vm.viewModelScope.cancel()
    }

    @Test
    fun `play again resets state`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)

        vm.onIntent(MathChallengeIntent.DigitEntered(8))
        vm.onIntent(MathChallengeIntent.Submit)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)

        assertTrue(vm.state.value.score > 0)

        vm.onIntent(MathChallengeIntent.PlayAgain)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)

        assertEquals(0, vm.state.value.score)
        assertEquals(0, vm.state.value.streak)
        assertEquals(MathChallengeState.INITIAL_LIVES, vm.state.value.lives)
        assertEquals(MathChallengeState.INITIAL_BOMBS, vm.state.value.bombs)
        assertFalse(vm.state.value.isGameOver)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `spawned equations have valid xOffset`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        for (eq in vm.state.value.equations) {
            assertTrue(
                eq.xOffset >= MathChallengeViewModel.X_MARGIN,
                "xOffset ${eq.xOffset} below margin",
            )
            assertTrue(
                eq.xOffset <= MathChallengeViewModel.X_MARGIN + MathChallengeViewModel.X_RANGE,
                "xOffset ${eq.xOffset} above max",
            )
        }

        vm.viewModelScope.cancel()
    }

    @Test
    fun `spawned equations start above visible area`() = runTest(testDispatcher) {
        val vm = createViewModel()

        // Catch equations right after spawn before they fall far
        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        for (eq in vm.state.value.equations) {
            // Equation may have moved a few ticks but should still be near top
            assertTrue(
                eq.yProgress < 0.3f,
                "equation yProgress ${eq.yProgress} too far down at spawn",
            )
        }

        vm.viewModelScope.cancel()
    }

    @Test
    fun `calculateScore includes level bonus`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val eq = FallingEquation(id = 0, problem = createProblem(), yProgress = 0.3f)

        val scoreL1 = vm.calculateScore(eq, level = 1, multiplier = 1.0f)
        val scoreL5 = vm.calculateScore(eq, level = 5, multiplier = 1.0f)

        // Level 5 should score higher due to level bonus (+3 per level)
        assertEquals(
            (MathChallengeViewModel.LEVEL_SCORE_BONUS * 4),
            scoreL5 - scoreL1,
        )

        vm.viewModelScope.cancel()
    }

    @Test
    fun `calculateScore includes proximity bonus`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val eqSafe = FallingEquation(id = 0, problem = createProblem(), yProgress = 0.3f)
        val eqMid = FallingEquation(id = 1, problem = createProblem(), yProgress = 0.55f)
        val eqDanger = FallingEquation(id = 2, problem = createProblem(), yProgress = 0.75f)

        val scoreSafe = vm.calculateScore(eqSafe, level = 1, multiplier = 1.0f)
        val scoreMid = vm.calculateScore(eqMid, level = 1, multiplier = 1.0f)
        val scoreDanger = vm.calculateScore(eqDanger, level = 1, multiplier = 1.0f)

        assertTrue(scoreDanger > scoreMid, "danger zone should score more than mid")
        assertTrue(scoreMid > scoreSafe, "mid zone should score more than safe")

        vm.viewModelScope.cancel()
    }

    @Test
    fun `calculateScore caps multiplier at 1_5x`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val eq = FallingEquation(id = 0, problem = createProblem(), yProgress = 0.3f)

        val scoreCapped = vm.calculateScore(eq, level = 1, multiplier = 1.5f)
        val scoreHighMult = vm.calculateScore(eq, level = 1, multiplier = 3.0f)

        // Both should be the same because multiplier caps at 1.5x
        assertEquals(scoreCapped, scoreHighMult)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `missed equation costs a life and breaks streak`() = runTest(testDispatcher) {
        val vm = createViewModel()

        advanceTimeBy(spawnTimeMs)
        assertTrue(vm.state.value.equations.isNotEmpty())

        // Solve one to build streak
        vm.onIntent(MathChallengeIntent.DigitEntered(8))
        vm.onIntent(MathChallengeIntent.Submit)
        advanceTimeBy(MathChallengeViewModel.TICK_MS)
        assertEquals(1, vm.state.value.streak)
        val livesAfterSolve = vm.state.value.lives

        // Wait for another equation to spawn, then let it fall off screen
        // Spawn wait: ~3000ms (60 ticks), fall from -0.12 to 1.0: ~18700ms (374 ticks)
        // Total ~22000ms, use 25000 for safety margin
        advanceTimeBy(25_000L)

        assertTrue(
            vm.state.value.lives < livesAfterSolve,
            "lives should decrease when equation is missed",
        )
        assertEquals(0, vm.state.value.streak, "streak should reset when equation is missed")

        vm.viewModelScope.cancel()
    }

    @Test
    fun `game over when all lives lost`() = runTest(testDispatcher) {
        val vm = createViewModel()

        // Let equations fall off the bottom repeatedly until game over
        // Each miss costs 1 life. Start with INITIAL_LIVES (3).
        // Let enough time pass for many equations to spawn and miss.
        val longWait = 120_000L
        advanceTimeBy(longWait)

        assertTrue(vm.state.value.isGameOver, "game should be over after losing all lives")
        assertEquals(0, vm.state.value.lives)

        vm.viewModelScope.cancel()
    }
}
