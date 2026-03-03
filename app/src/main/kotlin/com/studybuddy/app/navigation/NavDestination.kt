package com.studybuddy.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CrueltyFree
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes

/**
 * All top-level navigation destinations. Phone uses a subset (PHONE_NAV_ITEMS),
 * tablet uses all learning + utility destinations.
 */
enum class NavDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(
        route = StudyBuddyRoutes.HOME,
        labelResId = CoreUiR.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    POEMS(
        route = StudyBuddyRoutes.POEMS,
        labelResId = CoreUiR.string.nav_poems,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
    ),
    DICTEE(
        route = StudyBuddyRoutes.DICTEE_LISTS,
        labelResId = CoreUiR.string.nav_dictee,
        selectedIcon = Icons.Filled.Draw,
        unselectedIcon = Icons.Outlined.Draw,
    ),
    SPEED_MATH(
        route = StudyBuddyRoutes.MATH_SETUP,
        labelResId = CoreUiR.string.nav_speed_math,
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
    ),
    MATH_CHALLENGE(
        route = StudyBuddyRoutes.MATH_CHALLENGE,
        labelResId = CoreUiR.string.nav_challenge,
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt,
    ),
    AVATAR(
        route = StudyBuddyRoutes.AVATAR,
        labelResId = CoreUiR.string.nav_avatar,
        selectedIcon = Icons.Filled.CrueltyFree,
        unselectedIcon = Icons.Outlined.CrueltyFree,
    ),
    STATS(
        route = StudyBuddyRoutes.STATS,
        labelResId = CoreUiR.string.nav_stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
    ),
    SETTINGS(
        route = StudyBuddyRoutes.SETTINGS,
        labelResId = CoreUiR.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
}

/** Subset shown in phone bottom bar (max 5 for Material 3 guidelines). */
val PHONE_NAV_DESTINATIONS = listOf(
    NavDestination.HOME,
    NavDestination.STATS,
    NavDestination.AVATAR,
    NavDestination.SETTINGS,
)

/** Full list shown in tablet rail / drawer. */
val TABLET_NAV_DESTINATIONS = NavDestination.entries.toList()

/** Learning section destinations (shown under "Learn" group in drawer). */
val LEARNING_DESTINATIONS = listOf(
    NavDestination.HOME,
    NavDestination.POEMS,
    NavDestination.DICTEE,
    NavDestination.SPEED_MATH,
    NavDestination.MATH_CHALLENGE,
)

/** Utility destinations (shown under separate group in drawer). */
val UTILITY_DESTINATIONS = listOf(
    NavDestination.AVATAR,
    NavDestination.STATS,
)

/** Routes on which the phone bottom nav is shown. */
val PHONE_NAV_ROUTES = PHONE_NAV_DESTINATIONS.map { it.route }.toSet()

/**
 * Resolves the active top-level destination from the current route.
 * Handles nested routes (e.g. "poems/detail/123" → POEMS).
 */
fun resolveActiveDestination(currentRoute: String?): NavDestination? = when {
    currentRoute == null -> null
    currentRoute == StudyBuddyRoutes.HOME -> NavDestination.HOME
    currentRoute.startsWith("poems") -> NavDestination.POEMS
    currentRoute.startsWith("dictee") -> NavDestination.DICTEE
    currentRoute == StudyBuddyRoutes.MATH_CHALLENGE -> NavDestination.MATH_CHALLENGE
    currentRoute.startsWith("math") -> NavDestination.SPEED_MATH
    currentRoute == StudyBuddyRoutes.AVATAR -> NavDestination.AVATAR
    currentRoute == StudyBuddyRoutes.REWARDS -> NavDestination.AVATAR
    currentRoute == StudyBuddyRoutes.STATS -> NavDestination.STATS
    currentRoute == StudyBuddyRoutes.SETTINGS -> NavDestination.SETTINGS
    currentRoute == StudyBuddyRoutes.BACKUP -> NavDestination.SETTINGS
    else -> null
}

/** Routes where the child is in an active practice session. */
private val ACTIVE_SESSION_PREFIXES = listOf(
    "dictee/practice/",
    "dictee/challenge/",
    "math/play/",
)

private val ACTIVE_SESSION_EXACT = setOf(
    StudyBuddyRoutes.MATH_CHALLENGE,
)

/** Returns true if the current route is an active practice/play session. */
fun isActiveSessionRoute(currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    if (currentRoute in ACTIVE_SESSION_EXACT) return true
    return ACTIVE_SESSION_PREFIXES.any { currentRoute.startsWith(it) }
}
