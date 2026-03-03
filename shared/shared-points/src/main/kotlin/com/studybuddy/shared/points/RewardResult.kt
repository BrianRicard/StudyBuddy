package com.studybuddy.shared.points

/**
 * Result of a reward calculation.
 *
 * @property basePoints Section-specific base points before multipliers.
 * @property difficultyMultiplier 1.0x to 3.0x based on how hard the settings are.
 * @property accuracyMultiplier 0.2x to 1.5x based on how well the child performed.
 * @property volumeBonus Small extra for doing more (longer poems, more problems).
 * @property totalPoints Final points awarded (always >= 1).
 * @property breakdown Detailed component breakdown for display.
 */
data class RewardResult(
    val basePoints: Int,
    val difficultyMultiplier: Float,
    val accuracyMultiplier: Float,
    val volumeBonus: Int,
    val totalPoints: Int,
    val breakdown: PointBreakdown,
)

/**
 * Detailed breakdown of how points were calculated.
 */
data class PointBreakdown(
    val base: Int,
    val difficultyBonus: Int,
    val accuracyBonus: Int,
    val volumeBonus: Int,
    val total: Int,
)
