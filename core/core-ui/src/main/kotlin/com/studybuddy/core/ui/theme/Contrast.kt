package com.studybuddy.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * WCAG 2.1 contrast ratio between two opaque colours, from 1.0 (identical) to
 * 21.0 (black on white).
 */
fun contrastRatio(
    foreground: Color,
    background: Color,
): Float {
    val a = foreground.luminance() + LUMINANCE_OFFSET
    val b = background.luminance() + LUMINANCE_OFFSET
    return maxOf(a, b) / minOf(a, b)
}

/**
 * Returns this colour darkened or lightened just enough to reach [minRatio]
 * against [background], keeping as much of the original hue as possible.
 *
 * The six app themes pick vivid brand colours for `primary` — Arctic's cyan
 * reaches only 2.3:1 on white — so anything that has to be *seen* rather than
 * merely decorative must be run through this rather than used raw.
 */
fun Color.ensureContrastWith(
    background: Color,
    minRatio: Float,
): Color {
    if (contrastRatio(this, background) >= minRatio) return this

    val target = if (background.luminance() > MID_LUMINANCE) Color.Black else Color.White
    var fraction = BLEND_STEP
    while (fraction < 1f) {
        val candidate = lerp(this, target, fraction)
        if (contrastRatio(candidate, background) >= minRatio) return candidate
        fraction += BLEND_STEP
    }
    return target
}

private const val LUMINANCE_OFFSET = 0.05f
private const val MID_LUMINANCE = 0.5f
private const val BLEND_STEP = 0.05f
