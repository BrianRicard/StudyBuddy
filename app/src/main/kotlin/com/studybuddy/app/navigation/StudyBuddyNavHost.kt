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
import com.studybuddy.feature.home.HomeScreen
import com.studybuddy.feature.math.play.MathPlayScreen
import com.studybuddy.feature.math.results.MathResultsScreen
import com.studybuddy.feature.math.setup.MathSetupScreen
import com.studybuddy.feature.onboarding.OnboardingScreen
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
                    navController.navigate(StudyBuddyRoutes.DICTEE_LISTS)
                },
                onNavigateToMath = {
                    navController.navigate(StudyBuddyRoutes.MATH_SETUP)
                },
                onNavigateToAvatar = {
                    navController.navigate(StudyBuddyRoutes.AVATAR)
                },
                onNavigateToStats = {
                    navController.navigate(StudyBuddyRoutes.STATS)
                },
                onNavigateToRewards = {
                    navController.navigate(StudyBuddyRoutes.REWARDS)
                },
                onNavigateToSettings = {
                    navController.navigate(StudyBuddyRoutes.SETTINGS)
                },
            )
        }

        // Dictée flow
        composable(route = StudyBuddyRoutes.DICTEE_LISTS) {
            com.studybuddy.feature.dictee.list.DicteeListScreen(
                onNavigateToWords = { listId ->
                    navController.navigate(StudyBuddyRoutes.dicteeWords(listId))
                },
            )
        }

        composable(
            route = StudyBuddyRoutes.DICTEE_PRACTICE,
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) {
            com.studybuddy.feature.dictee.practice.DicteePracticeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Math flow
        composable(route = StudyBuddyRoutes.MATH_SETUP) {
            MathSetupScreen(
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

        // Avatar Closet
        composable(route = StudyBuddyRoutes.AVATAR) {
            AvatarClosetScreen()
        }

        // Rewards Shop
        composable(route = StudyBuddyRoutes.REWARDS) {
            RewardsShopScreen()
        }

        // Stats
        composable(route = StudyBuddyRoutes.STATS) {
            StatsScreen()
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

        // Backup & Export
        composable(route = StudyBuddyRoutes.BACKUP) {
            BackupExportScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
