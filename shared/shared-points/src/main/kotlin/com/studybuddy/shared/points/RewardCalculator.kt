package com.studybuddy.shared.points

import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.InputMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Unified reward calculation engine for all four sections.
 *
 * Every section uses the same structure:
 * ```
 * totalPoints = floor(basePoints * difficultyMultiplier * accuracyMultiplier) + volumeBonus
 * ```
 *
 * Design goals:
 * - Effort = Reward (harder challenges earn proportionally more)
 * - All sections feel worthwhile (~15-25 pts per 5 min of medium effort)
 * - Economy has longevity (Starter avatars quick, Legendary takes weeks)
 * - Never punitive (minimum 1 point for any completed session)
 */
@Singleton
class RewardCalculator @Inject constructor() {

    fun calculate(input: RewardInput): RewardResult = when (input) {
        is RewardInput.PoemReward -> calculatePoemReward(input)
        is RewardInput.DicteeReward -> calculateDicteeReward(input)
        is RewardInput.SpeedMathReward -> calculateSpeedMathReward(input)
        is RewardInput.MathChallengeReward -> calculateChallengeReward(input)
    }

    internal fun calculatePoemReward(input: RewardInput.PoemReward): RewardResult {
        val base = POEM_BASE_POINTS

        val diffMultiplier = when {
            input.wordCount <= 20 -> 1.0f
            input.wordCount <= 50 -> 1.3f
            input.wordCount <= 100 -> 1.6f
            else -> 2.0f
        }

        val accMultiplier = when (input.starRating) {
            5 -> 1.5f
            4 -> 1.2f
            3 -> 1.0f
            2 -> 0.6f
            else -> 0.3f
        }

        val volumeBonus = when {
            input.completeness >= 0.90f -> 5
            input.completeness >= 0.70f -> 3
            input.completeness >= 0.50f -> 1
            else -> 0
        }

        val raw = (base * diffMultiplier * accMultiplier).toInt()
        val total = (raw + volumeBonus).coerceAtLeast(1)

        return RewardResult(
            basePoints = base,
            difficultyMultiplier = diffMultiplier,
            accuracyMultiplier = accMultiplier,
            volumeBonus = volumeBonus,
            totalPoints = total,
            breakdown = PointBreakdown(
                base = base,
                difficultyBonus = (base * diffMultiplier).toInt() - base,
                accuracyBonus = raw - (base * diffMultiplier).toInt(),
                volumeBonus = volumeBonus,
                total = total,
            ),
        )
    }

    internal fun calculateDicteeReward(input: RewardInput.DicteeReward): RewardResult {
        val base = (input.totalWords * DICTEE_POINTS_PER_WORD).coerceAtLeast(2)

        val diffBase = when (input.difficulty) {
            Difficulty.EASY -> 0.8f
            Difficulty.MEDIUM, Difficulty.ADAPTIVE -> 1.0f
            Difficulty.HARD -> 1.4f
        }
        val modeBonus = when (input.inputMode) {
            InputMode.HANDWRITING -> 0.2f
            else -> 0.0f
        }
        val diffMultiplier = diffBase + modeBonus

        val accuracy = input.correctWords.toFloat() / input.totalWords.coerceAtLeast(1)
        val accMultiplier = when {
            accuracy >= 0.95f -> 1.5f
            accuracy >= 0.80f -> 1.2f
            accuracy >= 0.60f -> 1.0f
            accuracy >= 0.40f -> 0.6f
            else -> 0.3f
        }

        val volumeBonus = when {
            input.totalWords >= 20 -> 5
            input.totalWords >= 10 -> 3
            input.totalWords >= 5 -> 1
            else -> 0
        }

        val raw = (base * diffMultiplier * accMultiplier).toInt()
        val total = (raw + volumeBonus).coerceAtLeast(1)

        return RewardResult(
            basePoints = base,
            difficultyMultiplier = diffMultiplier,
            accuracyMultiplier = accMultiplier,
            volumeBonus = volumeBonus,
            totalPoints = total,
            breakdown = PointBreakdown(
                base = base,
                difficultyBonus = (base * diffMultiplier).toInt() - base,
                accuracyBonus = raw - (base * diffMultiplier).toInt(),
                volumeBonus = volumeBonus,
                total = total,
            ),
        )
    }

    internal fun calculateSpeedMathReward(input: RewardInput.SpeedMathReward): RewardResult {
        val base = input.totalProblems.coerceAtLeast(1)

        // Factor 1: Time pressure
        val timeFactor = when (input.timeLimitSeconds) {
            null, 0 -> 0.5f
            in 91..Int.MAX_VALUE -> 0.7f
            in 46..90 -> 1.0f
            in 16..45 -> 1.3f
            in 1..15 -> 1.8f
            else -> 0.5f
        }

        // Factor 2: Operator complexity
        val opFactor = when (input.operators.size) {
            1 -> 0.8f
            2 -> 1.0f
            3 -> 1.2f
            4 -> 1.5f
            else -> if (input.operators.size >= 5) 1.5f else 0.8f
        }

        // Factor 3: Number range difficulty
        val rangeDiff = input.numberRangeMax - input.numberRangeMin
        val rangeFactor = when {
            rangeDiff <= 10 -> 0.7f
            rangeDiff <= 20 -> 1.0f
            rangeDiff <= 50 -> 1.2f
            rangeDiff <= 100 -> 1.5f
            else -> 1.8f
        }

        val diffMultiplier = (timeFactor * opFactor * rangeFactor).coerceIn(0.3f, 3.0f)

        val accuracy = input.correctAnswers.toFloat() / input.totalProblems.coerceAtLeast(1)
        val accMultiplier = when {
            accuracy >= 0.95f -> 1.5f
            accuracy >= 0.80f -> 1.2f
            accuracy >= 0.60f -> 1.0f
            accuracy >= 0.40f -> 0.6f
            else -> 0.3f
        }

        val volumeBonus = when {
            input.totalProblems >= 50 -> 8
            input.totalProblems >= 30 -> 5
            input.totalProblems >= 20 -> 3
            input.totalProblems >= 10 -> 1
            else -> 0
        }

        val raw = (base * diffMultiplier * accMultiplier).toInt()
        val total = (raw + volumeBonus).coerceAtLeast(1)

        return RewardResult(
            basePoints = base,
            difficultyMultiplier = diffMultiplier,
            accuracyMultiplier = accMultiplier,
            volumeBonus = volumeBonus,
            totalPoints = total,
            breakdown = PointBreakdown(
                base = base,
                difficultyBonus = (base * diffMultiplier).toInt() - base,
                accuracyBonus = raw - (base * diffMultiplier).toInt(),
                volumeBonus = volumeBonus,
                total = total,
            ),
        )
    }

    internal fun calculateChallengeReward(input: RewardInput.MathChallengeReward): RewardResult {
        val base = (input.score / 100f).roundToInt().coerceAtLeast(1)

        val diffMultiplier = when {
            input.highestLevel >= 8 -> 2.0f
            input.highestLevel >= 6 -> 1.6f
            input.highestLevel >= 4 -> 1.3f
            input.highestLevel >= 2 -> 1.0f
            else -> 0.7f
        }

        val survivalMinutes = input.timeSurvivedMs / 60_000f
        val accMultiplier = when {
            survivalMinutes >= 5f -> 1.5f
            survivalMinutes >= 3f -> 1.3f
            survivalMinutes >= 2f -> 1.1f
            survivalMinutes >= 1f -> 1.0f
            else -> 0.7f
        }

        val volumeBonus = when {
            input.longestStreak >= 15 -> 8
            input.longestStreak >= 10 -> 5
            input.longestStreak >= 5 -> 2
            else -> 0
        }

        val raw = (base * diffMultiplier * accMultiplier).toInt()
        val total = (raw + volumeBonus).coerceAtLeast(1)

        return RewardResult(
            basePoints = base,
            difficultyMultiplier = diffMultiplier,
            accuracyMultiplier = accMultiplier,
            volumeBonus = volumeBonus,
            totalPoints = total,
            breakdown = PointBreakdown(
                base = base,
                difficultyBonus = (base * diffMultiplier).toInt() - base,
                accuracyBonus = raw - (base * diffMultiplier).toInt(),
                volumeBonus = volumeBonus,
                total = total,
            ),
        )
    }

    companion object {
        const val POEM_BASE_POINTS = 10
        const val DICTEE_POINTS_PER_WORD = 2
    }
}
