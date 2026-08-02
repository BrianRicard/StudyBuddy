package com.studybuddy.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The avatar closet marks the equipped character with `primary`, which is a
 * different vivid brand colour in each of the six themes. Left raw, three of
 * them are too faint to see — so the correction is pinned here per theme.
 */
class ContrastTest {

    private val themes: List<Pair<String, ColorScheme>> = listOf(
        "Sunset" to SunsetColorScheme,
        "Ocean" to OceanColorScheme,
        "Forest" to ForestColorScheme,
        "Galaxy" to GalaxyColorScheme,
        "Candy" to CandyColorScheme,
        "Arctic" to ArcticColorScheme,
    )

    @Test
    fun `black on white is the maximum ratio`() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(21f, contrastRatio(Color.White, Color.Black), 0.01f)
    }

    @Test
    fun `a colour has no contrast with itself`() {
        assertEquals(1f, contrastRatio(Color.Red, Color.Red), 0.01f)
    }

    @Test
    fun `every theme's raw primary reaches readable contrast after correction`() {
        for ((name, scheme) in themes) {
            val corrected = scheme.primary.ensureContrastWith(scheme.surface, TEXT_RATIO)
            val ratio = contrastRatio(corrected, scheme.surface)
            assertTrue(ratio >= TEXT_RATIO, "$name: corrected primary on surface is only $ratio")
        }
    }

    @Test
    fun `the corrected accent also stands out against the page background`() {
        // The ring is drawn on the card edge, so it borders the background too.
        for ((name, scheme) in themes) {
            val corrected = scheme.primary.ensureContrastWith(scheme.surface, TEXT_RATIO)
            val ratio = contrastRatio(corrected, scheme.background)
            assertTrue(ratio >= GRAPHIC_RATIO, "$name: corrected primary on background is only $ratio")
        }
    }

    @Test
    fun `the tick stays visible on its own chip`() {
        for ((name, scheme) in themes) {
            val chip = scheme.primary.ensureContrastWith(scheme.surface, TEXT_RATIO)
            val ratio = contrastRatio(scheme.onPrimary, chip)
            assertTrue(ratio >= GRAPHIC_RATIO, "$name: tick on chip is only $ratio")
        }
    }

    @Test
    fun `a colour that already passes is returned untouched`() {
        val corrected = Color.Black.ensureContrastWith(Color.White, TEXT_RATIO)
        assertEquals(Color.Black, corrected)
    }

    @Test
    fun `correction lightens rather than darkens on a dark background`() {
        val onDark = Color(0xFF3A2A6A).ensureContrastWith(Color(0xFF16213E), TEXT_RATIO)
        assertTrue(
            onDark.luminanceValue() > Color(0xFF3A2A6A).luminanceValue(),
            "a dark background should push the accent lighter",
        )
    }

    private fun Color.luminanceValue() = contrastRatio(this, Color.Black)

    private companion object {
        /** WCAG AA for body text. */
        const val TEXT_RATIO = 4.5f

        /** WCAG AA for graphical objects such as the ring and the tick. */
        const val GRAPHIC_RATIO = 3f
    }
}
