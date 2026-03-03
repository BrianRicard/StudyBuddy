package com.studybuddy.feature.math.setup

import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Operator
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MathSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MathSetupViewModel()

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(setOf(Operator.PLUS, Operator.MINUS), state.selectedOperators)
        assertEquals(AppConstants.MIN_NUMBER_RANGE, state.numberRangeMin)
        assertEquals(MathSetupState.DEFAULT_RANGE_MAX, state.numberRangeMax)
        assertEquals(MathSetupState.DEFAULT_TIMER_SECONDS, state.timerSeconds)
        assertEquals(MathSetupState.DEFAULT_PROBLEM_COUNT, state.problemCount)
        assertFalse(state.isCustomRange)
    }

    @Test
    fun `toggle operator adds operator when not selected`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.MULTIPLY))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(Operator.MULTIPLY in state.selectedOperators)
        assertTrue(Operator.PLUS in state.selectedOperators)
        assertTrue(Operator.MINUS in state.selectedOperators)
        assertEquals(3, state.selectedOperators.size)
    }

    @Test
    fun `toggle operator removes operator when selected and not last`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Remove PLUS (MINUS still selected)
        viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.PLUS))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(Operator.PLUS in state.selectedOperators)
        assertTrue(Operator.MINUS in state.selectedOperators)
        assertEquals(1, state.selectedOperators.size)
    }

    @Test
    fun `toggle operator does not remove last operator`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Remove PLUS first
        viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.PLUS))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.selectedOperators.size)

        // Try to remove the last operator (MINUS)
        viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.MINUS))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(Operator.MINUS in state.selectedOperators)
        assertEquals(1, state.selectedOperators.size)
    }

    @Test
    fun `set range min updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SetRangeMin(5))
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.numberRangeMin)
    }

    @Test
    fun `set range min clamps to max minus one`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Try to set min above the current max (20)
        viewModel.onIntent(MathSetupIntent.SetRangeMin(25))
        advanceUntilIdle()

        assertEquals(19, viewModel.state.value.numberRangeMin)
    }

    @Test
    fun `set range max updates state with no hard upper limit`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SetRangeMax(500))
        advanceUntilIdle()

        assertEquals(500, viewModel.state.value.numberRangeMax)
    }

    @Test
    fun `set range max clamps to min plus one`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SetRangeMax(0))
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.numberRangeMax)
    }

    @Test
    fun `select range preset updates range and clears custom`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SelectCustomRange)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isCustomRange)

        viewModel.onIntent(MathSetupIntent.SelectRangePreset(1, 50))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.numberRangeMin)
        assertEquals(50, state.numberRangeMax)
        assertFalse(state.isCustomRange)
    }

    @Test
    fun `select custom range enables custom mode`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SelectCustomRange)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isCustomRange)
    }

    @Test
    fun `set timer updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SetTimer(30))
        advanceUntilIdle()

        assertEquals(30, viewModel.state.value.timerSeconds)
    }

    @Test
    fun `set problem count updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MathSetupIntent.SetProblemCount(50))
        advanceUntilIdle()

        assertEquals(50, viewModel.state.value.problemCount)
    }
}
