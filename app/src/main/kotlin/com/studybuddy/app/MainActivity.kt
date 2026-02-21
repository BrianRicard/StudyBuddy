package com.studybuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.studybuddy.app.navigation.StudyBuddyNavHost
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.ui.navigation.StudyBuddyRoutes
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.ThemeConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeId by settingsRepository.getSelectedTheme()
                .collectAsState(initial = "sunset")
            val isOnboardingComplete by settingsRepository.isOnboardingComplete()
                .collectAsState(initial = true)

            val themeConfig = ThemeConfig.fromId(themeId)
            val startDestination = if (isOnboardingComplete) {
                StudyBuddyRoutes.HOME
            } else {
                StudyBuddyRoutes.ONBOARDING
            }

            StudyBuddyTheme(themeConfig = themeConfig) {
                val navController = rememberNavController()
                StudyBuddyNavHost(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }
}
