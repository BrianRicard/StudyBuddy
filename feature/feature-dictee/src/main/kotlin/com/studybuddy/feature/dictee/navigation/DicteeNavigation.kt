package com.studybuddy.feature.dictee.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.core.ui.navigation.navigateSafely
import com.studybuddy.feature.dictee.add.AddDicteeScreen
import com.studybuddy.feature.dictee.list.DicteeListScreen
import com.studybuddy.feature.dictee.practice.DicteePracticeScreen

fun NavGraphBuilder.dicteeNavGraph(navController: NavController) {
    navigation(
        startDestination = StudyBuddyRoutes.DICTEE_LISTS,
        route = "dictee",
    ) {
        composable(StudyBuddyRoutes.DICTEE_LISTS) {
            DicteeListScreen(
                onNavigateToPractice = { listId ->
                    navController.navigateSafely(StudyBuddyRoutes.dicteePractice(listId))
                },
                onNavigateToChallenge = { listIds ->
                    navController.navigateSafely(StudyBuddyRoutes.dicteeChallenge(listIds))
                },
                onNavigateToAdd = {
                    navController.navigateSafely(StudyBuddyRoutes.DICTEE_ADD)
                },
                onNavigateToEdit = { setId ->
                    navController.navigateSafely(StudyBuddyRoutes.dicteeEdit(setId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(StudyBuddyRoutes.DICTEE_ADD) {
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
