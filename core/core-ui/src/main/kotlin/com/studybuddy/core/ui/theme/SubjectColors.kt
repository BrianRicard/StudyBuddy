package com.studybuddy.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/**
 * A subject hue resolved against the theme the child is currently using.
 *
 * The hue itself is fixed on purpose — she learns "purple means Dictée", and that
 * should not move when she buys a new theme. What *cannot* be fixed is the tint
 * behind it: a hand-picked pale tint is invisible under Galaxy, which is a dark
 * theme, so the tint is mixed from the hue over the live surface instead.
 */
@Immutable
data class SubjectPalette(
    /** The hue itself, corrected until it is visible on [container]. */
    val accent: Color,
    /** The soft ground the accent is drawn on. */
    val container: Color,
    /** Text drawn on [container]. */
    val onContainer: Color,
)

/**
 * Resolves [hue] against the current theme.
 *
 * [onSurfaceRatio] is the bar the label must clear: 4.5:1 for text, 3:1 for icons
 * and other graphical objects (WCAG 2.1).
 */
@Composable
fun subjectPalette(hue: Color): SubjectPalette {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    return remember(hue, surface, onSurface) {
        val container = hue.copy(alpha = CONTAINER_ALPHA).compositeOver(surface)
        SubjectPalette(
            accent = hue.ensureContrastWith(container, GRAPHICAL_MIN_RATIO),
            container = container,
            onContainer = onSurface.ensureContrastWith(container, TEXT_MIN_RATIO),
        )
    }
}

/** WCAG 2.1 minimum for body text. */
const val TEXT_MIN_RATIO = 4.5f

/** WCAG 2.1 minimum for icons and other graphical objects. */
const val GRAPHICAL_MIN_RATIO = 3f

/** Enough tint to read as "this card is the maths one", not enough to fight the text. */
private const val CONTAINER_ALPHA = 0.14f
