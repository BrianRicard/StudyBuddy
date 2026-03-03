package com.studybuddy.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import com.studybuddy.app.navigation.StudyBuddyNavHost
import com.studybuddy.app.navigation.StudyBuddyScaffold
import com.studybuddy.core.domain.repository.SettingsRepository
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
                        return@StudyBuddyTheme
                    }

                    val startDestination = if (isOnboardingComplete == true) {
                        StudyBuddyRoutes.HOME
                    } else {
                        StudyBuddyRoutes.ONBOARDING
                    }

                    val navController = rememberNavController()

                    StudyBuddyScaffold(
                        navController = navController,
                        layoutType = layoutType,
                    ) { modifier ->
                        StudyBuddyNavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}
