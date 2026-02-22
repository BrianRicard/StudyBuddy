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

    private fun createViewModel() =
        MathSetupViewModel()

    @Test
    fun `initial state has default values`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(setOf(Operator.PLUS), state.selectedOperators)
            assertEquals(AppConstants.MIN_NUMBER_RANGE, state.numberRangeMin)
            assertEquals(12, state.numberRangeMax)
            assertEquals(AppConstants.DEFAULT_TIMER_SECONDS, state.timerSeconds)
            assertEquals(AppConstants.DEFAULT_PROBLEM_COUNT, state.problemCount)
        }

    @Test
    fun `toggle operator adds operator when not selected`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.MINUS))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(Operator.MINUS in state.selectedOperators)
            assertTrue(Operator.PLUS in state.selectedOperators)
            assertEquals(2, state.selectedOperators.size)
        }

    @Test
    fun `toggle operator removes operator when selected and not last`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Add MINUS first
            viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.MINUS))
            advanceUntilIdle()
            assertEquals(2, viewModel.state.value.selectedOperators.size)

            // Remove PLUS
            viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.PLUS))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(Operator.PLUS in state.selectedOperators)
            assertTrue(Operator.MINUS in state.selectedOperators)
            assertEquals(1, state.selectedOperators.size)
        }

    @Test
    fun `toggle operator does not remove last operator`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Try to remove the only selected operator
            viewModel.onIntent(MathSetupIntent.ToggleOperator(Operator.PLUS))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(Operator.PLUS in state.selectedOperators)
            assertEquals(1, state.selectedOperators.size)
        }

    @Test
    fun `set range min updates state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.SetRangeMin(5))
            advanceUntilIdle()

            assertEquals(5, viewModel.state.value.numberRangeMin)
        }

    @Test
    fun `set range min clamps to max`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Try to set min above the current max (12)
            viewModel.onIntent(MathSetupIntent.SetRangeMin(15))
            advanceUntilIdle()

            assertEquals(12, viewModel.state.value.numberRangeMin)
        }

    @Test
    fun `set range max updates state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.SetRangeMax(15))
            advanceUntilIdle()

            assertEquals(15, viewModel.state.value.numberRangeMax)
        }

    @Test
    fun `set range max clamps to max setup range`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.SetRangeMax(100))
            advanceUntilIdle()

            assertEquals(MathSetupViewModel.MAX_SETUP_RANGE, viewModel.state.value.numberRangeMax)
        }

    @Test
    fun `set timer updates state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.SetTimer(30))
            advanceUntilIdle()

            assertEquals(30, viewModel.state.value.timerSeconds)
        }

    @Test
    fun `set problem count updates state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(MathSetupIntent.SetProblemCount(50))
            advanceUntilIdle()

            assertEquals(50, viewModel.state.value.problemCount)
        }
}
