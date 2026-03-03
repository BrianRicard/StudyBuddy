package com.studybuddy.shared.points

import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.domain.model.Operator

/**
 * Input data for the unified reward calculator. One variant per section.
 */
sealed class RewardInput {

    data class PoemReward(
        val starRating: Int,
        val accuracy: Float,
        val completeness: Float,
        val wordCount: Int,
        val language: String,
    ) : RewardInput()

    data class DicteeReward(
        val correctWords: Int,
        val totalWords: Int,
        val inputMode: InputMode,
        val difficulty: Difficulty,
        val averageSimilarity: Float,
    ) : RewardInput()

    data class SpeedMathReward(
        val correctAnswers: Int,
        val totalProblems: Int,
        val timeLimitSeconds: Int?,
        val operators: Set<Operator>,
        val numberRangeMin: Int,
        val numberRangeMax: Int,
        val averageResponseTimeMs: Long,
    ) : RewardInput()

    data class MathChallengeReward(
        val score: Int,
        val solvedCount: Int,
        val timeSurvivedMs: Long,
        val highestLevel: Int,
        val longestStreak: Int,
    ) : RewardInput()
}
