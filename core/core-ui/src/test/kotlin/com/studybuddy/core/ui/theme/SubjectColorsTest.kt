package com.studybuddy.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The Home screen paints six fixed subject hues across six themes, one of which
 * (Galaxy) is dark. Hardcoded pale tints made the whole screen white-on-white
 * there, so the resolution is arithmetic now — and pinned here, per hue per theme.
 *
 * These mirror `subjectPalette()`, which cannot be called outside a composition.
 */
class SubjectColorsTest {

    private val themes: List<Pair<String, ColorScheme>> = listOf(
        "Sunset" to SunsetColorScheme,
        "Ocean" to OceanColorScheme,
        "Forest" to ForestColorScheme,
        "Galaxy" to GalaxyColorScheme,
        "Candy" to CandyColorScheme,
        "Arctic" to ArcticColorScheme,
    )

    private val hues: List<Pair<String, Color>> = listOf(
        "Dictee" to SubjectDictee,
        "Math" to SubjectMath,
        "Verbs" to SubjectVerbs,
        "Poems" to SubjectPoems,
        "Reading" to SubjectReading,
        "Arcade" to SubjectArcade,
        "Streak" to StreakOrange,
        "Points" to PointsGold,
    )

    private fun container(
        hue: Color,
        scheme: ColorScheme,
    ) = hue.copy(alpha = CONTAINER_ALPHA).compositeOver(scheme.surface)

    @Test
    fun `card labels are readable on every subject tint in every theme`() {
        forEachCombination { name, hue, scheme ->
            val container = container(hue, scheme)
            val label = scheme.onSurface.ensureContrastWith(container, TEXT_MIN_RATIO)
            val ratio = contrastRatio(label, container)
            assertTrue(ratio >= TEXT_MIN_RATIO, "$name: label on tint is only $ratio")
        }
    }

    @Test
    fun `subject icons stay visible on their own tint in every theme`() {
        forEachCombination { name, hue, scheme ->
            val container = container(hue, scheme)
            val accent = hue.ensureContrastWith(container, GRAPHICAL_MIN_RATIO)
            val ratio = contrastRatio(accent, container)
            assertTrue(ratio >= GRAPHICAL_MIN_RATIO, "$name: icon on tint is only $ratio")
        }
    }

    @Test
    fun `the due-count badge is readable on the surface it sits on`() {
        forEachCombination { name, hue, scheme ->
            val badge = hue.ensureContrastWith(scheme.surface, TEXT_MIN_RATIO)
            val ratio = contrastRatio(badge, scheme.surface)
            assertTrue(ratio >= TEXT_MIN_RATIO, "$name: badge on surface is only $ratio")
        }
    }

    @Test
    fun `the Start label is readable on every theme's primary`() {
        // The label is `onPrimary` (white in every theme), so the fill is what moves.
        for ((name, scheme) in themes) {
            val fill = scheme.primary.ensureContrastWith(scheme.onPrimary, TEXT_MIN_RATIO)
            val ratio = contrastRatio(scheme.onPrimary, fill)
            assertTrue(ratio >= TEXT_MIN_RATIO, "$name: Start label on primary is only $ratio")
        }
    }

    @Test
    fun `the tint is a visible shift away from the plain surface`() {
        // If the tint collapsed into the surface, the cards would lose the colour
        // coding the child navigates by, while still passing every contrast test.
        forEachCombination { name, hue, scheme ->
            val ratio = contrastRatio(container(hue, scheme), scheme.surface)
            assertTrue(ratio > 1.02f, "$name: tint is indistinguishable from the surface")
        }
    }

    private fun forEachCombination(assertion: (String, Color, ColorScheme) -> Unit) {
        for ((themeName, scheme) in themes) {
            for ((hueName, hue) in hues) {
                assertion("$themeName/$hueName", hue, scheme)
            }
        }
    }

    private companion object {
        /** Must match `SubjectColors.kt`. */
        const val CONTAINER_ALPHA = 0.14f
    }
}
