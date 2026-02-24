package com.studybuddy.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Sunset (Default)
private val SunsetPrimary = Color(0xFFFF6B4A)
private val SunsetSecondary = Color(0xFFFF9A76)
private val SunsetTertiary = Color(0xFFFFD166)
private val SunsetBackground = Color(0xFFFFF8F0)
private val SunsetSurface = Color(0xFFFFFFFF)
private val SunsetOnPrimary = Color(0xFFFFFFFF)
private val SunsetOnBackground = Color(0xFF2D2D2D)

val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    onPrimary = SunsetOnPrimary,
    onBackground = SunsetOnBackground,
    onSurface = SunsetOnBackground,
)

// Ocean
private val OceanPrimary = Color(0xFF4A90D9)
private val OceanSecondary = Color(0xFF64B5F6)
private val OceanTertiary = Color(0xFF81D4FA)
private val OceanBackground = Color(0xFFF0F7FF)

val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1A237E),
    onSurface = Color(0xFF1A237E),
)

// Forest
private val ForestPrimary = Color(0xFF4CAF50)
private val ForestSecondary = Color(0xFF81C784)
private val ForestTertiary = Color(0xFFA5D6A7)
private val ForestBackground = Color(0xFFF1F8E9)

val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20),
)

// Galaxy
private val GalaxyPrimary = Color(0xFF7C4DFF)
private val GalaxySecondary = Color(0xFFB388FF)
private val GalaxyTertiary = Color(0xFFEA80FC)
private val GalaxyBackground = Color(0xFF1A1A2E)

val GalaxyColorScheme = darkColorScheme(
    primary = GalaxyPrimary,
    secondary = GalaxySecondary,
    tertiary = GalaxyTertiary,
    background = GalaxyBackground,
    surface = Color(0xFF16213E),
    onPrimary = Color.White,
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
)

// Candy
private val CandyPrimary = Color(0xFFE91E63)
private val CandySecondary = Color(0xFFF48FB1)
private val CandyTertiary = Color(0xFFCE93D8)
private val CandyBackground = Color(0xFFFFF0F5)

val CandyColorScheme = lightColorScheme(
    primary = CandyPrimary,
    secondary = CandySecondary,
    tertiary = CandyTertiary,
    background = CandyBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF880E4F),
    onSurface = Color(0xFF880E4F),
)

// Arctic
private val ArcticPrimary = Color(0xFF00BCD4)
private val ArcticSecondary = Color(0xFF4DD0E1)
private val ArcticTertiary = Color(0xFF80DEEA)
private val ArcticBackground = Color(0xFFE0F7FA)

val ArcticColorScheme = lightColorScheme(
    primary = ArcticPrimary,
    secondary = ArcticSecondary,
    tertiary = ArcticTertiary,
    background = ArcticBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF006064),
    onSurface = Color(0xFF006064),
)

// Shared colors
val CorrectGreen = Color(0xFF4CAF50)
val IncorrectRed = Color(0xFFE57373)
val TimeoutAmber = Color(0xFFFFA726)
val StreakOrange = Color(0xFFFF9800)
val PointsGold = Color(0xFFFFD700)
