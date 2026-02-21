package com.studybuddy.feature.math.setup

import androidx.lifecycle.ViewModel
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.Operator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MathSetupState(
    val selectedOperators: Set<Operator> = setOf(Operator.PLUS),
    val numberRangeMin: Int = AppConstants.MIN_NUMBER_RANGE,
    val numberRangeMax: Int = 12,
    val timerSeconds: Int = AppConstants.DEFAULT_TIMER_SECONDS,
    val problemCount: Int = AppConstants.DEFAULT_PROBLEM_COUNT,
    val difficulty: Difficulty = Difficulty.ADAPTIVE,
)

sealed interface MathSetupIntent {
    data class ToggleOperator(val operator: Operator) : MathSetupIntent
    data class SetRangeMin(val min: Int) : MathSetupIntent
    data class SetRangeMax(val max: Int) : MathSetupIntent
    data class SetTimer(val seconds: Int) : MathSetupIntent
    data class SetProblemCount(val count: Int) : MathSetupIntent
}

@HiltViewModel
class MathSetupViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(MathSetupState())
    val state: StateFlow<MathSetupState> = _state.asStateFlow()

    fun onIntent(intent: MathSetupIntent) {
        when (intent) {
            is MathSetupIntent.ToggleOperator -> toggleOperator(intent.operator)
            is MathSetupIntent.SetRangeMin -> setRangeMin(intent.min)
            is MathSetupIntent.SetRangeMax -> setRangeMax(intent.max)
            is MathSetupIntent.SetTimer -> setTimer(intent.seconds)
            is MathSetupIntent.SetProblemCount -> setProblemCount(intent.count)
        }
    }

    private fun toggleOperator(operator: Operator) {
        _state.update { current ->
            val updated = if (operator in current.selectedOperators) {
                // Prevent deselecting the last operator — at least one must remain
                if (current.selectedOperators.size > 1) {
                    current.selectedOperators - operator
                } else {
                    current.selectedOperators
                }
            } else {
                current.selectedOperators + operator
            }
            current.copy(selectedOperators = updated)
        }
    }

    private fun setRangeMin(min: Int) {
        _state.update { current ->
            val clamped = min.coerceIn(
                minimumValue = AppConstants.MIN_NUMBER_RANGE,
                maximumValue = current.numberRangeMax,
            )
            current.copy(numberRangeMin = clamped)
        }
    }

    private fun setRangeMax(max: Int) {
        _state.update { current ->
            val clamped = max.coerceIn(
                minimumValue = current.numberRangeMin,
                maximumValue = MAX_SETUP_RANGE,
            )
            current.copy(numberRangeMax = clamped)
        }
    }

    private fun setTimer(seconds: Int) {
        _state.update { it.copy(timerSeconds = seconds) }
    }

    private fun setProblemCount(count: Int) {
        _state.update { it.copy(problemCount = count) }
    }

    companion object {
        /** Maximum value for the number range slider on the setup screen. */
        const val MAX_SETUP_RANGE = 17
    }
}
