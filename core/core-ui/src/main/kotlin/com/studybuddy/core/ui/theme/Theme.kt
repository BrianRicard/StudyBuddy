package com.studybuddy.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun StudyBuddyTheme(
    themeConfig: ThemeConfig = ThemeConfig.Sunset,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeConfig) {
        ThemeConfig.Sunset -> SunsetColorScheme
        ThemeConfig.Ocean -> OceanColorScheme
        ThemeConfig.Forest -> ForestColorScheme
        ThemeConfig.Galaxy -> GalaxyColorScheme
        ThemeConfig.Candy -> CandyColorScheme
        ThemeConfig.Arctic -> ArcticColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudyBuddyTypography,
        shapes = StudyBuddyShapes,
        content = content,
    )
}
