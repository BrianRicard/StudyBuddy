package com.studybuddy.feature.math.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.usecase.math.GenerateProblemUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

data class FallingEquation(
    val id: Int,
    val problem: MathProblem,
    val yProgress: Float = 0f,
    val isRainbow: Boolean = false,
    val xProgress: Float = 0f,
)

data class MathChallengeState(
    val equations: List<FallingEquation> = emptyList(),
    val userAnswer: String = "",
    val lives: Int = INITIAL_LIVES,
    val bombs: Int = INITIAL_BOMBS,
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val consecutiveCorrect: Int = 0,
    val level: Int = 1,
    val equationsSolved: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val showBombFlash: Boolean = false,
) {
    val multiplier: Float
        get() = when {
            streak >= STREAK_3X -> 3.0f
            streak >= STREAK_2X -> 2.0f
            streak >= STREAK_1_5X -> 1.5f
            else -> 1.0f
        }

    companion object {
        const val INITIAL_LIVES = 3
        const val INITIAL_BOMBS = 2
        const val STREAK_1_5X = 5
        const val STREAK_2X = 10
        const val STREAK_3X = 20
    }
}

sealed interface MathChallengeIntent {
    data class DigitEntered(val digit: Int) : MathChallengeIntent
    data object Backspace : MathChallengeIntent
    data object Submit : MathChallengeIntent
    data object UseBomb : MathChallengeIntent
    data object Pause : MathChallengeIntent
    data object Resume : MathChallengeIntent
    data object PlayAgain : MathChallengeIntent
}

sealed interface MathChallengeEffect {
    data object EquationSolved : MathChallengeEffect
    data object EquationMissed : MathChallengeEffect
    data object BombUsed : MathChallengeEffect
    data object LifeGained : MathChallengeEffect
    data class StreakMilestone(val streak: Int) : MathChallengeEffect
    data object GameOver : MathChallengeEffect
}

@HiltViewModel
class MathChallengeViewModel @Inject constructor(
    private val generateProblem: GenerateProblemUseCase,
    private val awardPoints: AwardPointsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MathChallengeState())
    val state: StateFlow<MathChallengeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MathChallengeEffect>()
    val effects: SharedFlow<MathChallengeEffect> = _effects.asSharedFlow()

    private var gameLoopJob: Job? = null
    private var nextEquationId = 0
    private var ticksSinceLastSpawn = 0

    init {
        startGame()
    }

    fun onIntent(intent: MathChallengeIntent) {
        when (intent) {
            is MathChallengeIntent.DigitEntered -> handleDigit(intent.digit)
            is MathChallengeIntent.Backspace -> handleBackspace()
            is MathChallengeIntent.Submit -> handleSubmit()
            is MathChallengeIntent.UseBomb -> handleBomb()
            is MathChallengeIntent.Pause -> _state.update { it.copy(isPaused = true) }
            is MathChallengeIntent.Resume -> _state.update { it.copy(isPaused = false) }
            is MathChallengeIntent.PlayAgain -> restartGame()
        }
    }

    private fun startGame() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            delay(INITIAL_DELAY_MS)

            while (true) {
                delay(TICK_MS)

                val current = _state.value
                if (current.isGameOver || current.isPaused) continue

                ticksSinceLastSpawn++
                val level = (current.equationsSolved / EQUATIONS_PER_LEVEL) + 1

                // Spawn new equations
                val spawnInterval = spawnIntervalForLevel(level)
                if (ticksSinceLastSpawn >= spawnInterval &&
                    current.equations.size < maxEquationsForLevel(level)
                ) {
                    spawnEquation(level)
                    ticksSinceLastSpawn = 0
                }

                // Move equations
                val fallSpeed = fallSpeedForLevel(level)
                val updated = _state.value.equations.map { eq ->
                    if (eq.isRainbow) {
                        eq.copy(
                            yProgress = eq.yProgress + fallSpeed * RAINBOW_SPEED_FACTOR,
                            xProgress = (eq.xProgress + RAINBOW_X_SPEED).coerceAtMost(1f),
                        )
                    } else {
                        eq.copy(yProgress = eq.yProgress + fallSpeed)
                    }
                }

                val missed = updated.filter { it.yProgress >= 1f }
                val remaining = updated.filter { it.yProgress < 1f }
                val newLives = (current.lives - missed.size).coerceAtLeast(0)

                _state.update {
                    it.copy(
                        equations = remaining,
                        lives = newLives,
                        level = level,
                        streak = if (missed.isNotEmpty()) 0 else it.streak,
                        consecutiveCorrect = if (missed.isNotEmpty()) 0 else it.consecutiveCorrect,
                    )
                }

                if (missed.isNotEmpty()) {
                    _effects.emit(MathChallengeEffect.EquationMissed)
                }

                if (newLives <= 0) {
                    endGame()
                }
            }
        }
    }

    private fun spawnEquation(level: Int) {
        val operators = operatorsForLevel(level)
        val range = rangeForLevel(level)
        val isRainbow = level >= RAINBOW_MIN_LEVEL &&
            Math.random() < rainbowChanceForLevel(level)

        val problem = generateProblem(
            operators = operators,
            range = range,
            difficulty = if (isRainbow) Difficulty.HARD else Difficulty.ADAPTIVE,
            currentStreak = _state.value.streak,
        )

        val equation = FallingEquation(
            id = nextEquationId++,
            problem = problem,
            isRainbow = isRainbow,
        )

        _state.update { it.copy(equations = it.equations + equation) }
    }

    private fun handleDigit(digit: Int) {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        if (current.userAnswer.length >= MAX_ANSWER_LENGTH) return

        _state.update { it.copy(userAnswer = it.userAnswer + digit.toString()) }
    }

    private fun handleBackspace() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        if (current.userAnswer.isNotEmpty()) {
            _state.update { it.copy(userAnswer = it.userAnswer.dropLast(1)) }
        }
    }

    private fun handleSubmit() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        if (current.userAnswer.isEmpty()) return

        val answerInt = current.userAnswer.toIntOrNull()

        if (answerInt != null) {
            // Find the bottom-most equation that matches
            val match = current.equations
                .sortedByDescending { it.yProgress }
                .firstOrNull { it.problem.correctAnswer == answerInt }

            if (match != null) {
                solveEquation(match)
            }
        }

        _state.update { it.copy(userAnswer = "") }
    }

    private fun solveEquation(equation: FallingEquation) {
        viewModelScope.launch {
            val current = _state.value
            val newStreak = current.streak + 1
            val newBestStreak = maxOf(current.bestStreak, newStreak)
            val newConsecutive = current.consecutiveCorrect + 1
            val newSolved = current.equationsSolved + 1

            val rainbowMult = if (equation.isRainbow) RAINBOW_SCORE_MULTIPLIER else 1
            val points = (BASE_POINTS_PER_SOLVE * rainbowMult * current.multiplier).toInt()

            // Life regen after 5 consecutive correct
            var newLives = current.lives
            var actualConsecutive = newConsecutive
            if (newConsecutive >= CONSECUTIVE_FOR_LIFE && newLives < MAX_LIVES) {
                newLives++
                actualConsecutive = 0
                _effects.emit(MathChallengeEffect.LifeGained)
            }

            // Rainbow equations give +1 bomb
            var newBombs = current.bombs
            if (equation.isRainbow) {
                newBombs++
            }

            _state.update {
                it.copy(
                    equations = it.equations.filter { eq -> eq.id != equation.id },
                    score = it.score + points,
                    streak = newStreak,
                    bestStreak = newBestStreak,
                    consecutiveCorrect = actualConsecutive,
                    equationsSolved = newSolved,
                    lives = newLives,
                    bombs = newBombs,
                )
            }

            _effects.emit(MathChallengeEffect.EquationSolved)

            if (newStreak == STREAK_MILESTONE_5 ||
                newStreak == STREAK_MILESTONE_10 ||
                newStreak == STREAK_MILESTONE_20
            ) {
                _effects.emit(MathChallengeEffect.StreakMilestone(newStreak))
            }
        }
    }

    private fun handleBomb() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        if (current.bombs <= 0 || current.equations.isEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    equations = emptyList(),
                    bombs = it.bombs - 1,
                    showBombFlash = true,
                )
            }
            _effects.emit(MathChallengeEffect.BombUsed)
            delay(BOMB_FLASH_MS)
            _state.update { it.copy(showBombFlash = false) }
        }
    }

    private fun endGame() {
        gameLoopJob?.cancel()
        viewModelScope.launch {
            val current = _state.value

            try {
                awardPoints(
                    profileId = AppConstants.DEFAULT_PROFILE_ID,
                    basePoints = current.score,
                    streak = 0,
                    source = PointSource.MATH,
                    reason = "Math Challenge: ${current.equationsSolved} solved, level ${current.level}",
                )
            } catch (_: Exception) {
                // Points award failure is not critical
            }

            _state.update { it.copy(isGameOver = true) }
            _effects.emit(MathChallengeEffect.GameOver)
        }
    }

    private fun restartGame() {
        nextEquationId = 0
        ticksSinceLastSpawn = 0
        _state.value = MathChallengeState()
        startGame()
    }

    // --- Difficulty scaling ---

    internal fun operatorsForLevel(level: Int): Set<Operator> = when {
        level >= LEVEL_ALL_OPS -> setOf(
            Operator.PLUS,
            Operator.MINUS,
            Operator.MULTIPLY,
            Operator.DIVIDE,
        )
        level >= LEVEL_ADD_MULTIPLY -> setOf(Operator.PLUS, Operator.MINUS, Operator.MULTIPLY)
        else -> setOf(Operator.PLUS, Operator.MINUS)
    }

    internal fun rangeForLevel(level: Int): IntRange {
        val max = (BASE_RANGE_MAX + (level - 1) * RANGE_INCREMENT).coerceAtMost(MAX_RANGE)
        return 1..max
    }

    internal fun fallSpeedForLevel(level: Int): Float = BASE_FALL_SPEED + (level - 1) * FALL_SPEED_INCREMENT

    internal fun spawnIntervalForLevel(level: Int): Int = (BASE_SPAWN_INTERVAL - (level - 1) * SPAWN_INTERVAL_DECREASE)
        .coerceAtLeast(MIN_SPAWN_INTERVAL)

    internal fun maxEquationsForLevel(level: Int): Int =
        (BASE_MAX_EQUATIONS + level / 2).coerceAtMost(MAX_EQUATIONS_ON_SCREEN)

    private fun rainbowChanceForLevel(level: Int): Double =
        (BASE_RAINBOW_CHANCE + (level - RAINBOW_MIN_LEVEL) * RAINBOW_CHANCE_INCREMENT)
            .coerceAtMost(MAX_RAINBOW_CHANCE)

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    companion object {
        const val TICK_MS = 50L
        const val INITIAL_DELAY_MS = 1500L
        const val BASE_FALL_SPEED = 0.003f
        const val FALL_SPEED_INCREMENT = 0.001f
        const val BASE_SPAWN_INTERVAL = 60 // ~3 seconds
        const val SPAWN_INTERVAL_DECREASE = 5
        const val MIN_SPAWN_INTERVAL = 20 // ~1 second
        const val BASE_MAX_EQUATIONS = 3
        const val MAX_EQUATIONS_ON_SCREEN = 6
        const val EQUATIONS_PER_LEVEL = 10
        const val MAX_ANSWER_LENGTH = 6
        const val BASE_POINTS_PER_SOLVE = 10
        const val RAINBOW_SCORE_MULTIPLIER = 3
        const val RAINBOW_SPEED_FACTOR = 0.7f
        const val RAINBOW_X_SPEED = 0.002f
        const val CONSECUTIVE_FOR_LIFE = 5
        const val MAX_LIVES = 5
        const val BOMB_FLASH_MS = 300L
        const val RAINBOW_MIN_LEVEL = 3
        const val BASE_RAINBOW_CHANCE = 0.1
        const val RAINBOW_CHANCE_INCREMENT = 0.05
        const val MAX_RAINBOW_CHANCE = 0.25
        const val BASE_RANGE_MAX = 10
        const val RANGE_INCREMENT = 5
        const val MAX_RANGE = 50
        const val LEVEL_ADD_MULTIPLY = 3
        const val LEVEL_ALL_OPS = 5
        const val STREAK_MILESTONE_5 = 5
        const val STREAK_MILESTONE_10 = 10
        const val STREAK_MILESTONE_20 = 20
    }
}
