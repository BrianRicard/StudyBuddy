package com.studybuddy.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CrueltyFree
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studybuddy.app.navigation.StudyBuddyNavHost
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.adaptive.LocalLayoutType
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.ThemeConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val layoutType = when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> LayoutType.COMPACT
                WindowWidthSizeClass.Medium -> LayoutType.MEDIUM
                WindowWidthSizeClass.Expanded -> LayoutType.EXPANDED
                else -> LayoutType.COMPACT
            }

            val themeId by settingsRepository.getSelectedTheme()
                .collectAsState(initial = "sunset")
            val isOnboardingComplete by settingsRepository.isOnboardingComplete()
                .collectAsState(initial = null)
            val locale by settingsRepository.getAppLocale()
                .collectAsState(initial = null)

            LaunchedEffect(locale) {
                val targetLocale = locale ?: return@LaunchedEffect
                val appLocales = AppCompatDelegate.getApplicationLocales()
                val currentLang = if (!appLocales.isEmpty) {
                    appLocales[0]?.toLanguageTag()
                } else {
                    null
                }
                if (targetLocale != currentLang) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(targetLocale),
                    )
                }
            }

            val themeConfig = ThemeConfig.fromId(themeId)

            CompositionLocalProvider(LocalLayoutType provides layoutType) {
            StudyBuddyTheme(themeConfig = themeConfig) {
                // Wait for DataStore to load before deciding start destination
                if (isOnboardingComplete == null) {
                    // Show nothing while loading — avoids wrong-screen flash
                    return@StudyBuddyTheme
                }

                val startDestination = if (isOnboardingComplete == true) {
                    StudyBuddyRoutes.HOME
                } else {
                    StudyBuddyRoutes.ONBOARDING
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                val showNav = currentRoute in BOTTOM_NAV_ROUTES

                // Hide navigation entirely on non-nav screens (onboarding, dictée practice, etc.)
                val navSuiteType = if (showNav) {
                    when (layoutType) {
                        LayoutType.COMPACT -> NavigationSuiteType.NavigationBar
                        LayoutType.MEDIUM,
                        LayoutType.EXPANDED -> NavigationSuiteType.NavigationRail
                    }
                } else {
                    NavigationSuiteType.None
                }

                NavigationSuiteScaffold(
                    layoutType = navSuiteType,
                    navigationSuiteItems = {
                        BOTTOM_NAV_ITEMS.forEach { navItem ->
                            val isSelected = currentDestination?.hierarchy?.any {
                                it.route == navItem.route
                            } == true
                            item(
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) {
                                            navItem.selectedIcon
                                        } else {
                                            navItem.unselectedIcon
                                        },
                                        contentDescription = stringResource(navItem.labelResId),
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                label = { Text(stringResource(navItem.labelResId)) },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(navItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                ) {
                    StudyBuddyNavHost(
                        navController = navController,
                        startDestination = startDestination,
                    )
                }
            }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    @StringRes val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(
        route = StudyBuddyRoutes.HOME,
        labelResId = CoreUiR.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        route = StudyBuddyRoutes.STATS,
        labelResId = CoreUiR.string.nav_stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
    ),
    BottomNavItem(
        route = StudyBuddyRoutes.AVATAR,
        labelResId = CoreUiR.string.nav_avatar,
        selectedIcon = Icons.Filled.CrueltyFree,
        unselectedIcon = Icons.Outlined.CrueltyFree,
    ),
    BottomNavItem(
        route = StudyBuddyRoutes.SETTINGS,
        labelResId = CoreUiR.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)

private val BOTTOM_NAV_ROUTES = BOTTOM_NAV_ITEMS.map { it.route }.toSet()
