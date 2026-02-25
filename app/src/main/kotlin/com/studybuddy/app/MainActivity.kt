package com.studybuddy.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studybuddy.app.navigation.StudyBuddyNavHost
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.ThemeConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it },
                        ) {
                            StudyBuddyBottomNav(navController = navController)
                        }
                    },
                ) { padding ->
                    StudyBuddyNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        contentPadding = padding,
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int = 0,
)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(StudyBuddyRoutes.HOME, CoreUiR.string.nav_home, Icons.Default.Home),
    BottomNavItem(StudyBuddyRoutes.STATS, CoreUiR.string.nav_stats, Icons.Default.Star),
    BottomNavItem(
        StudyBuddyRoutes.REWARDS,
        CoreUiR.string.nav_rewards,
        iconResId = CoreUiR.drawable.ic_rewards_illustration,
    ),
    BottomNavItem(StudyBuddyRoutes.AVATAR, CoreUiR.string.nav_avatar, Icons.Outlined.Face),
    BottomNavItem(StudyBuddyRoutes.SETTINGS, CoreUiR.string.nav_settings, Icons.Default.Settings),
)

private val BOTTOM_NAV_ROUTES = BOTTOM_NAV_ITEMS.map { it.route }.toSet()

@Composable
private fun StudyBuddyBottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    if (item.iconResId != 0) {
                        Icon(
                            painter = painterResource(item.iconResId),
                            contentDescription = stringResource(item.labelResId),
                            tint = Color.Unspecified,
                        )
                    } else {
                        Icon(item.icon!!, contentDescription = stringResource(item.labelResId))
                    }
                },
                label = { Text(stringResource(item.labelResId)) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
