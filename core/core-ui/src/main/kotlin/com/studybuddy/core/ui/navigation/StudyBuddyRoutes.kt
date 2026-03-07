package com.studybuddy.core.ui.navigation

/**
 * Navigation routes for the StudyBuddy app.
 * The actual NavHost composable is wired up in the app module (Phase 6).
 */
object StudyBuddyRoutes {
    const val HOME = "home"
    const val DICTEE_LISTS = "dictee/lists"
    const val DICTEE_PRACTICE = "dictee/practice/{listId}"

    // listIds = pipe-separated UUIDs, e.g. "uuid1|uuid2|uuid3"
    const val DICTEE_CHALLENGE = "dictee/challenge/{listIds}"
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
    const val POEM_DETAIL = "poems/detail/{poemId}"
    const val POEM_CREATE = "poems/create"
    const val MATH_CHALLENGE = "math/challenge"
    const val DICTEE_ADD = "dictee/add"
    const val DICTEE_EDIT = "dictee/edit/{setId}"
    const val READING = "reading"
    const val READING_DETAIL = "reading/detail/{passageId}"
    const val READING_QUESTIONS =
        "reading/questions/{passageId}/{readingTimeMs}"
    const val READING_RESULTS =
        "reading/results/{passageId}/{score}/{totalQuestions}/{readingTimeMs}" +
            "/{questionsTimeMs}/{allCorrectFirstTry}/{tier}"

    fun poemDetail(poemId: String) = "poems/detail/$poemId"
    fun dicteeEdit(setId: String) = "dictee/edit/$setId"
    const val ONBOARDING = "onboarding"

    fun readingDetail(passageId: String) = "reading/detail/$passageId"
    fun readingQuestions(
        passageId: String,
        readingTimeMs: Long,
    ) = "reading/questions/$passageId/$readingTimeMs"

    fun readingResults(
        passageId: String,
        score: Int,
        totalQuestions: Int,
        readingTimeMs: Long,
        questionsTimeMs: Long,
        allCorrectFirstTry: Boolean,
        tier: Int,
    ) = "reading/results/$passageId/$score/$totalQuestions/$readingTimeMs" +
        "/$questionsTimeMs/$allCorrectFirstTry/$tier"

    fun dicteePractice(listId: String) = "dictee/practice/$listId"
    fun dicteeChallenge(listIds: List<String>) = "dictee/challenge/${listIds.joinToString("|")}"

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
