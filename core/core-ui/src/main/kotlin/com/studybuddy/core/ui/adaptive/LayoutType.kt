package com.studybuddy.core.ui.adaptive

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Represents the current layout classification based on window width.
 * Used to adapt UI between phone and tablet form factors.
 */
enum class LayoutType {
    /** Phone portrait/landscape (<600dp) */
    COMPACT,

    /** Tablet portrait or small tablet landscape (600dp–839dp) */
    MEDIUM,

    /** Tablet landscape (>=840dp) */
    EXPANDED,
}

/**
 * CompositionLocal providing the current [LayoutType] based on window size class.
 * Defaults to [LayoutType.COMPACT] so all existing phone layouts work unchanged.
 */
val LocalLayoutType = staticCompositionLocalOf { LayoutType.COMPACT }
