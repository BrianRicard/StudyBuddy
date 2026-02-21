package com.studybuddy.feature.math.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.usecase.math.SaveMathSessionUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.points.PointsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

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
    val isSaved: Boolean = false,
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
    private val saveMathSession: SaveMathSessionUseCase,
    private val awardPoints: AwardPointsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val totalProblems: Int = savedStateHandle["totalProblems"] ?: 0
    private val correctCount: Int = savedStateHandle["correctCount"] ?: 0
    private val bestStreak: Int = savedStateHandle["bestStreak"] ?: 0
    private val avgResponseMs: Long = savedStateHandle["avgResponseMs"] ?: 0L
    private val sessionScore: Int = savedStateHandle["sessionScore"] ?: 0
    private val operators: String = savedStateHandle["operators"] ?: ""
    private val rangeMin: Int = savedStateHandle["rangeMin"] ?: 0
    private val rangeMax: Int = savedStateHandle["rangeMax"] ?: 12

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

        saveSessionAndAwardPoints(totalPoints = totalPoints)
    }

    private fun saveSessionAndAwardPoints(totalPoints: Int) {
        viewModelScope.launch {
            val parsedOperators = operators
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { name ->
                    runCatching { Operator.valueOf(name.trim()) }.getOrNull()
                }
                .toSet()

            val session = MathSession(
                id = UUID.randomUUID().toString(),
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                operators = parsedOperators,
                numberRange = rangeMin..rangeMax,
                totalProblems = totalProblems,
                correctCount = correctCount,
                bestStreak = bestStreak,
                avgResponseMs = avgResponseMs,
                difficulty = Difficulty.ADAPTIVE,
                completedAt = Clock.System.now(),
            )

            saveMathSession(session)

            awardPoints(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = totalPoints,
                streak = bestStreak,
                source = PointSource.MATH,
                reason = "Speed Math: $correctCount/$totalProblems correct",
            )

            _state.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        private const val SPEED_DEMON_THRESHOLD_MS = 3000L
        private const val STREAK_MASTER_THRESHOLD = 10

        const val BADGE_SPEED_DEMON = "Speed Demon"
        const val BADGE_STREAK_MASTER = "Streak Master"
    }
}
