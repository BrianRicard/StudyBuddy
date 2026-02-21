package com.studybuddy.feature.math.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.usecase.math.CheckAnswerUseCase
import com.studybuddy.core.domain.usecase.math.GenerateProblemUseCase
import com.studybuddy.core.domain.usecase.math.SaveMathSessionUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.points.PointsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

data class MathPlayState(
    val currentProblem: MathProblem? = null,
    val userAnswer: String = "",
    val feedback: Feedback? = null,
    val problemsCompleted: Int = 0,
    val totalProblems: Int = 20,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val sessionScore: Int = 0,
    val timeRemainingMs: Long = 15_000,
    val timerTotal: Long = 15_000,
    val isPaused: Boolean = false,
    val isComplete: Boolean = false,
    val responseTimesMs: List<Long> = emptyList(),
    val showCelebration: Boolean = false,
    val pointsAwarded: Int = 0,
)

sealed interface MathPlayIntent {
    data class DigitEntered(val digit: Int) : MathPlayIntent
    data object Backspace : MathPlayIntent
    data object Submit : MathPlayIntent
    data object Pause : MathPlayIntent
    data object Resume : MathPlayIntent
}

sealed interface MathPlayEffect {
    data class ShowFeedback(val feedback: Feedback) : MathPlayEffect
    data class StreakMilestone(val streak: Int) : MathPlayEffect
    data object GameComplete : MathPlayEffect
}

@HiltViewModel
class MathPlayViewModel @Inject constructor(
    private val generateProblem: GenerateProblemUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val saveMathSession: SaveMathSessionUseCase,
    private val awardPoints: AwardPointsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val operators: Set<Operator>
    private val numberRange: IntRange
    private val difficulty: Difficulty
    private val timerMs: Long
    private val profileId: String = "default-profile"

    private val _state = MutableStateFlow(MathPlayState())
    val state: StateFlow<MathPlayState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MathPlayEffect>()
    val effects: SharedFlow<MathPlayEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var problemStartTimeMs: Long = 0L

    init {
        val operatorsArg: String = checkNotNull(savedStateHandle["operators"])
        operators = operatorsArg.split(",")
            .map { it.trim() }
            .map { name -> Operator.valueOf(name) }
            .toSet()

        val rangeMin: Int = checkNotNull(savedStateHandle["rangeMin"])
        val rangeMax: Int = checkNotNull(savedStateHandle["rangeMax"])
        numberRange = rangeMin..rangeMax

        val timerSeconds: Int = savedStateHandle["timerSeconds"] ?: DEFAULT_TIMER_SECONDS
        timerMs = timerSeconds * 1_000L

        val problemCount: Int = savedStateHandle["problemCount"] ?: DEFAULT_PROBLEM_COUNT

        difficulty = savedStateHandle.get<String>("difficulty")
            ?.let { Difficulty.valueOf(it) }
            ?: Difficulty.ADAPTIVE

        _state.update {
            it.copy(
                totalProblems = problemCount,
                timerTotal = timerMs,
                timeRemainingMs = timerMs,
            )
        }

        loadNextProblem()
    }

    fun onIntent(intent: MathPlayIntent) {
        when (intent) {
            is MathPlayIntent.DigitEntered -> handleDigitEntered(intent.digit)
            is MathPlayIntent.Backspace -> handleBackspace()
            is MathPlayIntent.Submit -> handleSubmit()
            is MathPlayIntent.Pause -> handlePause()
            is MathPlayIntent.Resume -> handleResume()
        }
    }

    private fun handleDigitEntered(digit: Int) {
        val currentState = _state.value
        if (currentState.feedback != null || currentState.isComplete || currentState.isPaused) return

        if (currentState.userAnswer.length >= MAX_ANSWER_LENGTH) return

        _state.update { it.copy(userAnswer = it.userAnswer + digit.toString()) }
    }

    private fun handleBackspace() {
        val currentState = _state.value
        if (currentState.feedback != null || currentState.isComplete || currentState.isPaused) return

        if (currentState.userAnswer.isNotEmpty()) {
            _state.update { it.copy(userAnswer = it.userAnswer.dropLast(1)) }
        }
    }

    private fun handleSubmit() {
        val currentState = _state.value
        val problem = currentState.currentProblem ?: return
        if (currentState.feedback != null || currentState.isComplete) return

        val userAnswerText = currentState.userAnswer.trim()
        if (userAnswerText.isEmpty()) return

        val userAnswerInt = userAnswerText.toIntOrNull() ?: return

        stopTimer()
        val responseTime = System.currentTimeMillis() - problemStartTimeMs
        val feedback = checkAnswer(problem, userAnswerInt)

        processAnswer(feedback, responseTime)
    }

    private fun handleTimeUp() {
        val currentState = _state.value
        if (currentState.feedback != null || currentState.isComplete) return

        stopTimer()
        val responseTime = currentState.timerTotal
        val feedback = Feedback.TimeUp

        processAnswer(feedback, responseTime)
    }

    private fun processAnswer(feedback: Feedback, responseTimeMs: Long) {
        viewModelScope.launch {
            val currentState = _state.value
            val isCorrect = feedback is Feedback.Correct
            val newStreak = if (isCorrect) currentState.streak + 1 else 0
            val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount
            val newBestStreak = maxOf(currentState.bestStreak, newStreak)
            val scoreIncrease = if (isCorrect) PointValues.MATH_CORRECT else 0
            val newResponseTimes = currentState.responseTimesMs + responseTimeMs

            _state.update {
                it.copy(
                    feedback = feedback,
                    streak = newStreak,
                    correctCount = newCorrectCount,
                    bestStreak = newBestStreak,
                    sessionScore = it.sessionScore + scoreIncrease,
                    responseTimesMs = newResponseTimes,
                    showCelebration = isCorrect && isStreakMilestone(newStreak),
                )
            }

            _effects.emit(MathPlayEffect.ShowFeedback(feedback))

            if (isCorrect && isStreakMilestone(newStreak)) {
                _effects.emit(MathPlayEffect.StreakMilestone(newStreak))
            }

            val feedbackDelay = if (isCorrect) CORRECT_FEEDBACK_DELAY_MS else INCORRECT_FEEDBACK_DELAY_MS
            delay(feedbackDelay)

            _state.update { it.copy(showCelebration = false) }

            val newProblemsCompleted = currentState.problemsCompleted + 1
            if (newProblemsCompleted >= currentState.totalProblems) {
                completeSession(
                    correctCount = newCorrectCount,
                    bestStreak = newBestStreak,
                    responseTimes = newResponseTimes,
                    totalProblems = currentState.totalProblems,
                )
            } else {
                _state.update { it.copy(problemsCompleted = newProblemsCompleted) }
                loadNextProblem()
            }
        }
    }

    private fun loadNextProblem() {
        val currentState = _state.value
        val problem = generateProblem(
            operators = operators,
            range = numberRange,
            difficulty = difficulty,
            currentStreak = currentState.streak,
        )

        _state.update {
            it.copy(
                currentProblem = problem,
                userAnswer = "",
                feedback = null,
                timeRemainingMs = timerMs,
            )
        }

        problemStartTimeMs = System.currentTimeMillis()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)

                val currentState = _state.value
                if (currentState.isPaused || currentState.feedback != null) continue

                val newTime = (currentState.timeRemainingMs - TIMER_TICK_MS).coerceAtLeast(0)
                _state.update { it.copy(timeRemainingMs = newTime) }

                if (newTime <= 0) {
                    handleTimeUp()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun handlePause() {
        _state.update { it.copy(isPaused = true) }
    }

    private fun handleResume() {
        _state.update { it.copy(isPaused = false) }
    }

    private fun completeSession(
        correctCount: Int,
        bestStreak: Int,
        responseTimes: List<Long>,
        totalProblems: Int,
    ) {
        stopTimer()

        viewModelScope.launch {
            val finalScore = PointsCalculator.calculateMathPoints(
                correctCount = correctCount,
                streak = bestStreak,
            )

            val awarded = awardPoints(
                profileId = profileId,
                basePoints = finalScore,
                streak = bestStreak,
                source = PointSource.MATH,
                reason = "Math session: $correctCount/$totalProblems correct",
            )

            val avgResponseMs = if (responseTimes.isNotEmpty()) {
                responseTimes.average().toLong()
            } else {
                0L
            }

            val session = MathSession(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                operators = operators,
                numberRange = numberRange,
                totalProblems = totalProblems,
                correctCount = correctCount,
                bestStreak = bestStreak,
                avgResponseMs = avgResponseMs,
                difficulty = difficulty,
                completedAt = Clock.System.now(),
            )
            saveMathSession(session)

            _state.update {
                it.copy(
                    isComplete = true,
                    problemsCompleted = totalProblems,
                    pointsAwarded = awarded,
                    feedback = null,
                )
            }

            _effects.emit(MathPlayEffect.GameComplete)
        }
    }

    private fun isStreakMilestone(streak: Int): Boolean =
        streak == STREAK_MILESTONE_5 || streak == STREAK_MILESTONE_10 || streak == STREAK_MILESTONE_20

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        private const val MAX_ANSWER_LENGTH = 6
        private const val TIMER_TICK_MS = 100L
        private const val CORRECT_FEEDBACK_DELAY_MS = 1_000L
        private const val INCORRECT_FEEDBACK_DELAY_MS = 2_000L
        private const val STREAK_MILESTONE_5 = 5
        private const val STREAK_MILESTONE_10 = 10
        private const val STREAK_MILESTONE_20 = 20
        private const val DEFAULT_TIMER_SECONDS = 15
        private const val DEFAULT_PROBLEM_COUNT = 20
    }
}
