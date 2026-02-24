package com.studybuddy.feature.dictee.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.feature.dictee.list.DicteeListScreen
import com.studybuddy.feature.dictee.practice.DicteePracticeScreen
import com.studybuddy.feature.dictee.words.DicteeWordEntryScreen

fun NavGraphBuilder.dicteeNavGraph(navController: NavController) {
    navigation(
        startDestination = StudyBuddyRoutes.DICTEE_LISTS,
        route = "dictee",
    ) {
        composable(StudyBuddyRoutes.DICTEE_LISTS) {
            DicteeListScreen(
                onNavigateToWords = { listId ->
                    navController.navigate(StudyBuddyRoutes.dicteeWords(listId))
                },
                onNavigateToChallenge = { listIds ->
                    navController.navigate(StudyBuddyRoutes.dicteeChallenge(listIds))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = StudyBuddyRoutes.DICTEE_WORDS,
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) {
            DicteeWordEntryScreen(
                onNavigateToPractice = { listId ->
                    navController.navigate(StudyBuddyRoutes.dicteePractice(listId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Single-list practice
        composable(
            route = StudyBuddyRoutes.DICTEE_PRACTICE,
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) {
            DicteePracticeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Multi-list challenge — same screen/ViewModel, receives pipe-separated listIds
        composable(
            route = StudyBuddyRoutes.DICTEE_CHALLENGE,
            arguments = listOf(navArgument("listIds") { type = NavType.StringType }),
        ) {
            DicteePracticeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
