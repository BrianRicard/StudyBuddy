package com.studybuddy.feature.math.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.studybuddy.shared.points.PointsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MathResultsState(
    val totalProblems: Int = 0,
    val correctCount: Int = 0,
    val bestStreak: Int = 0,
    val avgResponseMs: Long = 0,
    val sessionScore: Int = 0,
    val streakBonus: Int = 0,
    val totalPoints: Int = 0,
    val accuracy: Float = 0f,
    val badges: List<String> = emptyList(),
)

sealed interface MathResultsIntent {
    data object NavigateHome : MathResultsIntent
    data object PlayAgain : MathResultsIntent
}

sealed interface MathResultsEffect {
    data object NavigateToHome : MathResultsEffect
    data object NavigateToSetup : MathResultsEffect
}

@HiltViewModel
class MathResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val totalProblems: Int = savedStateHandle["totalProblems"] ?: 0
    private val correctCount: Int = savedStateHandle["correctCount"] ?: 0
    private val bestStreak: Int = savedStateHandle["bestStreak"] ?: 0
    private val avgResponseMs: Long = savedStateHandle["avgResponseMs"] ?: 0L
    private val sessionScore: Int = savedStateHandle["sessionScore"] ?: 0

    private val _state = MutableStateFlow(MathResultsState())
    val state: StateFlow<MathResultsState> = _state.asStateFlow()

    private val _effect = MutableStateFlow<MathResultsEffect?>(null)
    val effect: StateFlow<MathResultsEffect?> = _effect.asStateFlow()

    init {
        calculateResults()
    }

    fun onIntent(intent: MathResultsIntent) {
        when (intent) {
            is MathResultsIntent.NavigateHome -> {
                _effect.value = MathResultsEffect.NavigateToHome
            }
            is MathResultsIntent.PlayAgain -> {
                _effect.value = MathResultsEffect.NavigateToSetup
            }
        }
    }

    fun consumeEffect() {
        _effect.value = null
    }

    private fun calculateResults() {
        val accuracy = if (totalProblems > 0) {
            correctCount.toFloat() / totalProblems.toFloat()
        } else {
            0f
        }

        val streakBonus = PointsCalculator.calculateStreakBonus(bestStreak)
        val totalPoints = sessionScore + streakBonus

        val badges = buildList {
            if (avgResponseMs in 1 until SPEED_DEMON_THRESHOLD_MS) {
                add(BADGE_SPEED_DEMON)
            }
            if (bestStreak >= STREAK_MASTER_THRESHOLD) {
                add(BADGE_STREAK_MASTER)
            }
        }

        _state.update {
            it.copy(
                totalProblems = totalProblems,
                correctCount = correctCount,
                bestStreak = bestStreak,
                avgResponseMs = avgResponseMs,
                sessionScore = sessionScore,
                streakBonus = streakBonus,
                totalPoints = totalPoints,
                accuracy = accuracy,
                badges = badges,
            )
        }
    }

    companion object {
        private const val SPEED_DEMON_THRESHOLD_MS = 3000L
        private const val STREAK_MASTER_THRESHOLD = 10

        const val BADGE_SPEED_DEMON = "Speed Demon"
        const val BADGE_STREAK_MASTER = "Streak Master"
    }
}
