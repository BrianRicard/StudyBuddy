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

    // Poems
    const val POEM_READ_ALOUD = 10
    const val POEM_RECITED = 15
    const val POEM_GREAT_RECITATION = 25

    // Conjugation quest
    const val CONJUGATION_LISTEN_COMPLETE = 10
    const val CONJUGATION_FORM_WRITTEN = 10
    const val CONJUGATION_FORM_SPOKEN = 15
    const val CONJUGATION_FORM_ECHOED = 5
    const val CONJUGATION_BATTLE_WIN = 30
    const val CONJUGATION_BOSS_WIN = 50
    const val CONJUGATION_BOSS_SPOKEN_BONUS = 10
    const val CONJUGATION_STAGE_COMPLETE = 50
    const val CONJUGATION_PERFECT_BONUS = 25

    // Atelier des Verbes drills. Never zero — writing the word at all earns
    // something, even after a reveal-and-copy.
    const val CONJUGATION_DRILL_FIRST_TRY = 10
    const val CONJUGATION_DRILL_RETRY = 5
    const val CONJUGATION_DRILL_COPY = 2
    const val CONJUGATION_DRILL_STYLUS_BONUS = 5
    const val CONJUGATION_DRILL_SESSION_COMPLETE = 25

    // Jardin des Tables drills. Mirrors the Atelier ladder: answering at all
    // earns something, even after the answer was revealed.
    const val MATH_FACTS_FIRST_TRY = 10
    const val MATH_FACTS_RETRY = 5
    const val MATH_FACTS_COPY = 2
    const val MATH_FACTS_SESSION_COMPLETE = 25

    // General
    const val DAILY_LOGIN = 10
    const val FIRST_SESSION_OF_DAY = 20
    const val DAILY_CHALLENGE_COMPLETE = 100
    const val WEEKLY_CHALLENGE_COMPLETE = 200

    // Parent's plan
    /** Default award for finishing every task the parent set for the day; the parent can change it. */
    const val DEFAULT_PLAN_COMPLETION_BONUS = 40
    const val MAX_PLAN_COMPLETION_BONUS = 200

    /** Per day of an unbroken run, paid the following morning. */
    const val DAY_STREAK_BONUS_PER_DAY = 10

    /** The run keeps counting past this, but the bonus stops growing at 140. */
    const val MAX_REWARDED_STREAK_DAYS = 14

    /**
     * Bonus for showing up [streakDays] days running, paid the next morning.
     *
     * Unrelated to [streakMultiplier], which scales a single answer streak *inside*
     * a Speed Math session.
     */
    fun dayStreakBonus(streakDays: Int): Int =
        streakDays.coerceIn(0, MAX_REWARDED_STREAK_DAYS) * DAY_STREAK_BONUS_PER_DAY

    /**
     * Returns the multiplier for the current streak length.
     *
     * Streak 0–4:   ×1.0
     * Streak 5–9:   ×1.5
     * Streak 10–19: ×2.0
     * Streak 20+:   ×3.0
     */
    fun streakMultiplier(streak: Int): Double = when {
        streak < 5 -> 1.0
        streak < 10 -> 1.5
        streak < 20 -> 2.0
        else -> 3.0
    }
}
