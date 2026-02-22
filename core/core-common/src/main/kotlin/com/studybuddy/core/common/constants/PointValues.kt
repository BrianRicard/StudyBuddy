package com.studybuddy.core.common.constants

/**
 * All point values used across the StudyBuddy app.
 * Points are cosmetic only — no pay-to-win mechanics.
 */
object PointValues {
    // Dictée
    const val DICTEE_CORRECT_TYPED = 10
    const val DICTEE_CORRECT_HANDWRITTEN = 15
    const val DICTEE_PERFECT_LIST = 50

    // Math
    const val MATH_CORRECT = 5
    const val MATH_STREAK_5 = 25
    const val MATH_STREAK_10 = 75
    const val MATH_STREAK_20 = 150

    // General
    const val DAILY_LOGIN = 10
    const val FIRST_SESSION_OF_DAY = 20
    const val DAILY_CHALLENGE_COMPLETE = 100
    const val WEEKLY_CHALLENGE_COMPLETE = 200

    /**
     * Returns the multiplier for the current streak length.
     *
     * Streak 0–4:   ×1.0
     * Streak 5–9:   ×1.5
     * Streak 10–19: ×2.0
     * Streak 20+:   ×3.0
     */
    fun streakMultiplier(streak: Int): Double =
        when {
            streak < 5 -> 1.0
            streak < 10 -> 1.5
            streak < 20 -> 2.0
            else -> 3.0
        }
}
