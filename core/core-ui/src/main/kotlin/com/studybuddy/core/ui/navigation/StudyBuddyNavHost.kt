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
    const val MATH_PLAY =
        "math/play/{operators}/{rangeMin}/{rangeMax}/{timerSeconds}/{problemCount}"
    const val MATH_RESULTS =
        "math/results/{totalProblems}/{correctCount}/{bestStreak}/{avgResponseMs}" +
            "/{sessionScore}/{operators}/{rangeMin}/{rangeMax}"
    const val AVATAR = "avatar"
    const val REWARDS = "rewards"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val POEMS = "poems"
    const val ONBOARDING = "onboarding"

    fun dicteeWords(listId: String) = "dictee/words/$listId"
    fun dicteePractice(listId: String) = "dictee/practice/$listId"

    fun mathPlay(
        operators: String,
        rangeMin: Int,
        rangeMax: Int,
        timerSeconds: Int,
        problemCount: Int,
    ) = "math/play/$operators/$rangeMin/$rangeMax/$timerSeconds/$problemCount"

    fun mathResults(
        totalProblems: Int,
        correctCount: Int,
        bestStreak: Int,
        avgResponseMs: Long,
        sessionScore: Int,
        operators: String,
        rangeMin: Int,
        rangeMax: Int,
    ) = "math/results/$totalProblems/$correctCount/$bestStreak/$avgResponseMs" +
        "/$sessionScore/$operators/$rangeMin/$rangeMax"
}
