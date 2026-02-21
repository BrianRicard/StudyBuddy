package com.studybuddy.core.common.constants

object AppConstants {
    /**
     * Default profile ID used across the app. When multi-profile support is added,
     * replace usages with the active profile ID from [ProfileRepository.getActiveProfile].
     */
    const val DEFAULT_PROFILE_ID = "default"

    const val MAX_STREAK_DISPLAY = 999
    const val DEFAULT_DAILY_GOAL = 5
    const val MAX_DAILY_GOAL = 20
    const val DEFAULT_TIMER_SECONDS = 15
    const val MIN_TIMER_SECONDS = 5
    const val MAX_TIMER_SECONDS = 60
    const val DEFAULT_PROBLEM_COUNT = 20
    const val MAX_WORD_LENGTH = 50
    const val MIN_NUMBER_RANGE = 1
    const val MAX_NUMBER_RANGE = 100
    const val POWER_BASE_MIN = 2
    const val POWER_BASE_MAX = 5
    const val POWER_EXPONENT_MIN = 1
    const val POWER_EXPONENT_MAX = 3
    const val POWER_RESULT_MAX = 125
    const val DATABASE_NAME = "studybuddy.db"
    const val BACKUP_SCHEMA_VERSION = 1
}
