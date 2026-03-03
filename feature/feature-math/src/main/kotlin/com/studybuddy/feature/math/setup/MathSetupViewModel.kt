package com.studybuddy.feature.math.setup

import androidx.lifecycle.ViewModel
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.Operator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NumberRangePreset(
    val label: String,
    val min: Int,
    val max: Int,
)

data class MathSetupState(
    val selectedOperators: Set<Operator> = setOf(Operator.PLUS, Operator.MINUS),
    val numberRangeMin: Int = AppConstants.MIN_NUMBER_RANGE,
    val numberRangeMax: Int = DEFAULT_RANGE_MAX,
    val isCustomRange: Boolean = false,
    val timerSeconds: Int = DEFAULT_TIMER_SECONDS,
    val problemCount: Int = DEFAULT_PROBLEM_COUNT,
    val difficulty: Difficulty = Difficulty.ADAPTIVE,
) {
    companion object {
        const val DEFAULT_RANGE_MAX = 20
        const val DEFAULT_TIMER_SECONDS = 60
        const val DEFAULT_PROBLEM_COUNT = 10
    }
}

sealed interface MathSetupIntent {
    data class ToggleOperator(val operator: Operator) : MathSetupIntent
    data class SetRangeMin(val min: Int) : MathSetupIntent
    data class SetRangeMax(val max: Int) : MathSetupIntent
    data class SelectRangePreset(val min: Int, val max: Int) : MathSetupIntent
    data object SelectCustomRange : MathSetupIntent
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
            is MathSetupIntent.SelectRangePreset -> selectPreset(intent.min, intent.max)
            is MathSetupIntent.SelectCustomRange -> _state.update { it.copy(isCustomRange = true) }
            is MathSetupIntent.SetTimer -> setTimer(intent.seconds)
            is MathSetupIntent.SetProblemCount -> setProblemCount(intent.count)
        }
    }

    private fun toggleOperator(operator: Operator) {
        _state.update { current ->
            val updated = if (operator in current.selectedOperators) {
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
            val clamped = min.coerceIn(0, current.numberRangeMax - 1)
            current.copy(numberRangeMin = clamped)
        }
    }

    private fun setRangeMax(max: Int) {
        _state.update { current ->
            val clamped = max.coerceAtLeast(current.numberRangeMin + 1)
            current.copy(numberRangeMax = clamped)
        }
    }

    private fun selectPreset(
        min: Int,
        max: Int,
    ) {
        _state.update { it.copy(numberRangeMin = min, numberRangeMax = max, isCustomRange = false) }
    }

    private fun setTimer(seconds: Int) {
        _state.update { it.copy(timerSeconds = seconds) }
    }

    private fun setProblemCount(count: Int) {
        _state.update { it.copy(problemCount = count) }
    }

    companion object {
        val RANGE_PRESETS = listOf(
            NumberRangePreset("1-10", 1, 10),
            NumberRangePreset("1-20", 1, 20),
            NumberRangePreset("1-50", 1, 50),
            NumberRangePreset("1-100", 1, 100),
        )

        val TIMER_OPTIONS = listOf(
            15 to "15s",
            30 to "30s",
            60 to "1 min",
            90 to "1.5 min",
            120 to "2 min",
            0 to "\u221E",
        )

        val PROBLEM_COUNT_OPTIONS = listOf(5, 10, 20, 30, 50)
    }
}
