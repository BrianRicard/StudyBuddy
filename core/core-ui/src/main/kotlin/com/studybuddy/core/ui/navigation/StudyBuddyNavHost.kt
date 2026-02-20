package com.studybuddy.core.ui.navigation

/**
 * Navigation routes for the StudyBuddy app.
 * The actual NavHost composable is wired up in the app module (Phase 6).
 */
object StudyBuddyRoutes {
    const val HOME = "home"
    const val DICTEE_LISTS = "dictee/lists"
    const val DICTEE_WORDS = "dictee/words/{listId}"
    const val DICTEE_PRACTICE = "dictee/practice/{listId}"
    const val MATH_SETUP = "math/setup"
    const val MATH_PLAY = "math/play"
    const val MATH_RESULTS = "math/results"
    const val AVATAR = "avatar"
    const val REWARDS = "rewards"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val ONBOARDING = "onboarding"

    fun dicteeWords(listId: String) = "dictee/words/$listId"
    fun dicteePractice(listId: String) = "dictee/practice/$listId"
}
