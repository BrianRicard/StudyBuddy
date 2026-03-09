package com.studybuddy.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.feature.avatar.AvatarClosetScreen
import com.studybuddy.feature.backup.BackupExportScreen
import com.studybuddy.feature.dictee.add.AddDicteeScreen
import com.studybuddy.feature.dictee.list.DicteeListScreen
import com.studybuddy.feature.dictee.practice.DicteePracticeScreen
import com.studybuddy.feature.home.HomeScreen
import com.studybuddy.feature.math.challenge.MathChallengeScreen
import com.studybuddy.feature.math.play.MathPlayScreen
import com.studybuddy.feature.math.results.MathResultsScreen
import com.studybuddy.feature.math.setup.MathSetupScreen
import com.studybuddy.feature.onboarding.OnboardingScreen
import com.studybuddy.feature.poems.PoemDetailScreen
import com.studybuddy.feature.poems.PoemsScreen
import com.studybuddy.feature.poems.create.PoemCreateScreen
import com.studybuddy.feature.reading.detail.ReadingDetailScreen
import com.studybuddy.feature.reading.home.ReadingHomeScreen
import com.studybuddy.feature.reading.questions.QuestionsScreen
import com.studybuddy.feature.reading.results.ReadingResultsScreen
import com.studybuddy.feature.rewards.RewardsShopScreen
import com.studybuddy.feature.settings.SettingsScreen
import com.studybuddy.feature.stats.StatsScreen

private const val TRANSITION_DURATION = 300

@Composable
fun StudyBuddyNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(TRANSITION_DURATION),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(TRANSITION_DURATION),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(TRANSITION_DURATION),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(TRANSITION_DURATION),
            )
        },
    ) {
        // Onboarding
        composable(
            route = StudyBuddyRoutes.ONBOARDING,
            enterTransition = { fadeIn(tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) },
        ) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(StudyBuddyRoutes.HOME) {
                        popUpTo(StudyBuddyRoutes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        // Home
        composable(
            route = StudyBuddyRoutes.HOME,
            enterTransition = { fadeIn(tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) },
        ) {
            HomeScreen(
                onNavigateToDictee = {
                    navController.navigate(StudyBuddyRoutes.DICTEE_LISTS) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMath = {
                    navController.navigate(StudyBuddyRoutes.MATH_SETUP) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMathChallenge = {
                    navController.navigate(StudyBuddyRoutes.MATH_CHALLENGE) {
                        launchSingleTop = true
                    }
                },
                onNavigateToPoems = {
                    navController.navigate(StudyBuddyRoutes.POEMS) {
                        launchSingleTop = true
                    }
                },
                onNavigateToReading = {
                    navController.navigate(StudyBuddyRoutes.READING) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAvatar = {
                    navController.navigate(StudyBuddyRoutes.AVATAR) {
                        launchSingleTop = true
                    }
                },
                onNavigateToStats = {
                    navController.navigate(StudyBuddyRoutes.STATS) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRewards = {
                    navController.navigate(StudyBuddyRoutes.REWARDS) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(StudyBuddyRoutes.SETTINGS) {
                        launchSingleTop = true
                    }
                },
            )
        }

        // Dictée flow
        composable(route = StudyBuddyRoutes.DICTEE_LISTS) {
            DicteeListScreen(
                onNavigateToPractice = { listId ->
                    navController.navigate(StudyBuddyRoutes.dicteePractice(listId))
                },
                onNavigateToChallenge = { listIds ->
                    navController.navigate(StudyBuddyRoutes.dicteeChallenge(listIds))
                },
                onNavigateToAdd = {
                    navController.navigate(StudyBuddyRoutes.DICTEE_ADD)
                },
                onNavigateToEdit = { setId ->
                    navController.navigate(StudyBuddyRoutes.dicteeEdit(setId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(route = StudyBuddyRoutes.DICTEE_ADD) {
            AddDicteeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = StudyBuddyRoutes.DICTEE_EDIT,
            arguments = listOf(navArgument("setId") { type = NavType.StringType }),
        ) {
            AddDicteeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = StudyBuddyRoutes.DICTEE_PRACTICE,
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) {
            DicteePracticeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = StudyBuddyRoutes.DICTEE_CHALLENGE,
            arguments = listOf(navArgument("listIds") { type = NavType.StringType }),
        ) {
            DicteePracticeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Math flow
        composable(route = StudyBuddyRoutes.MATH_SETUP) {
            MathSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartGame = { setupState ->
                    val operatorsStr = setupState.selectedOperators
                        .joinToString(",") { it.name }
                    navController.navigate(
                        StudyBuddyRoutes.mathPlay(
                            operators = operatorsStr,
                            rangeMin = setupState.numberRangeMin,
                            rangeMax = setupState.numberRangeMax,
                            timerSeconds = setupState.timerSeconds,
                            problemCount = setupState.problemCount,
                        ),
                    )
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.MATH_PLAY,
            arguments = listOf(
                navArgument("operators") { type = NavType.StringType },
                navArgument("rangeMin") { type = NavType.IntType },
                navArgument("rangeMax") { type = NavType.IntType },
                navArgument("timerSeconds") { type = NavType.IntType },
                navArgument("problemCount") { type = NavType.IntType },
            ),
        ) {
            MathPlayScreen(
                onGameComplete = { total, correct, streak, avgMs, score, ops, rMin, rMax ->
                    navController.navigate(
                        StudyBuddyRoutes.mathResults(
                            totalProblems = total,
                            correctCount = correct,
                            bestStreak = streak,
                            avgResponseMs = avgMs,
                            sessionScore = score,
                            operators = ops,
                            rangeMin = rMin,
                            rangeMax = rMax,
                        ),
                    ) {
                        popUpTo(StudyBuddyRoutes.MATH_SETUP)
                    }
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.MATH_RESULTS,
            arguments = listOf(
                navArgument("totalProblems") { type = NavType.IntType },
                navArgument("correctCount") { type = NavType.IntType },
                navArgument("bestStreak") { type = NavType.IntType },
                navArgument("avgResponseMs") { type = NavType.LongType },
                navArgument("sessionScore") { type = NavType.IntType },
                navArgument("operators") { type = NavType.StringType },
                navArgument("rangeMin") { type = NavType.IntType },
                navArgument("rangeMax") { type = NavType.IntType },
            ),
        ) {
            MathResultsScreen(
                onPlayAgain = {
                    navController.navigate(StudyBuddyRoutes.MATH_SETUP) {
                        popUpTo(StudyBuddyRoutes.HOME)
                    }
                },
                onHome = {
                    navController.navigate(StudyBuddyRoutes.HOME) {
                        popUpTo(StudyBuddyRoutes.HOME) { inclusive = true }
                    }
                },
            )
        }

        // Math Challenge
        composable(route = StudyBuddyRoutes.MATH_CHALLENGE) {
            MathChallengeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(StudyBuddyRoutes.HOME) {
                        popUpTo(StudyBuddyRoutes.HOME) { inclusive = true }
                    }
                },
            )
        }

        // Avatar Closet
        composable(route = StudyBuddyRoutes.AVATAR) {
            AvatarClosetScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Rewards Shop
        composable(route = StudyBuddyRoutes.REWARDS) {
            RewardsShopScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Stats
        composable(route = StudyBuddyRoutes.STATS) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Settings
        composable(route = StudyBuddyRoutes.SETTINGS) {
            SettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onAppReset = {
                    navController.navigate(StudyBuddyRoutes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        // Poems
        composable(route = StudyBuddyRoutes.POEMS) {
            PoemsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { poemId ->
                    navController.navigate(StudyBuddyRoutes.poemDetail(poemId))
                },
                onNavigateToCreate = {
                    navController.navigate(StudyBuddyRoutes.POEM_CREATE)
                },
            )
        }

        composable(route = StudyBuddyRoutes.POEM_CREATE) {
            PoemCreateScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = StudyBuddyRoutes.POEM_DETAIL,
            arguments = listOf(navArgument("poemId") { type = NavType.StringType }),
        ) {
            PoemDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    navController.navigate(StudyBuddyRoutes.SETTINGS)
                },
            )
        }

        // Reading Comprehension
        composable(route = StudyBuddyRoutes.READING) {
            ReadingHomeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPassage = { passageId ->
                    navController.navigate(StudyBuddyRoutes.readingDetail(passageId))
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.READING_DETAIL,
            arguments = listOf(navArgument("passageId") { type = NavType.StringType }),
        ) {
            ReadingDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestions = { passageId, readingTimeMs ->
                    navController.navigate(
                        StudyBuddyRoutes.readingQuestions(passageId, readingTimeMs),
                    ) {
                        popUpTo(StudyBuddyRoutes.READING)
                    }
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.READING_QUESTIONS,
            arguments = listOf(
                navArgument("passageId") { type = NavType.StringType },
                navArgument("readingTimeMs") { type = NavType.LongType },
            ),
        ) {
            QuestionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResults = { passageId, score, total, readTime, qTime, firstTry, tier ->
                    navController.navigate(
                        StudyBuddyRoutes.readingResults(
                            passageId,
                            score,
                            total,
                            readTime,
                            qTime,
                            firstTry,
                            tier,
                        ),
                    ) {
                        popUpTo(StudyBuddyRoutes.READING)
                    }
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.READING_RESULTS,
            arguments = listOf(
                navArgument("passageId") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("readingTimeMs") { type = NavType.LongType },
                navArgument("questionsTimeMs") { type = NavType.LongType },
                navArgument("allCorrectFirstTry") { type = NavType.BoolType },
                navArgument("tier") { type = NavType.IntType },
            ),
        ) {
            ReadingResultsScreen(
                onNavigateToPassage = { passageId ->
                    navController.navigate(StudyBuddyRoutes.readingDetail(passageId)) {
                        popUpTo(StudyBuddyRoutes.READING)
                    }
                },
                onNavigateHome = {
                    navController.navigate(StudyBuddyRoutes.HOME) {
                        popUpTo(StudyBuddyRoutes.HOME) { inclusive = true }
                    }
                },
            )
        }

        // Backup & Export
        composable(route = StudyBuddyRoutes.BACKUP) {
            BackupExportScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
