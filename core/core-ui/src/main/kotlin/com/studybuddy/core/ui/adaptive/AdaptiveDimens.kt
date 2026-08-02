package com.studybuddy.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimension values that scale by [LayoutType] for adaptive layouts.
 */
data class AdaptiveDimens(
    val screenPadding: Dp,
    val cardPadding: Dp,
    val buttonHeight: Dp,
    val starSize: Dp,
    val canvasHeight: Dp,
    val contentMaxWidth: Dp,
    val speakerButtonSize: Dp,
    val letterCardSize: Dp,
    val progressBarHeight: Dp,
    val fabSize: Dp,
    /**
     * Narrowest a character/accessory grid cell may be. Grids use this with
     * `GridCells.Adaptive`, which adds columns to keep cells near this size —
     * so raising it on wider windows is what stops a tablet from stretching a
     * phone's three columns across the whole screen.
     */
    val avatarCellMinSize: Dp,
    /**
     * The single large avatar above the closet grid, capped against the screen
     * height at the call site: this is keyed to the *width* class, but a phone
     * in landscape is EXPANDED and only ~400dp tall.
     */
    val avatarHeroSize: Dp,
)

object AdaptiveDimensDefaults {

    private val compact = AdaptiveDimens(
        screenPadding = 16.dp,
        cardPadding = 12.dp,
        buttonHeight = 48.dp,
        starSize = 24.dp,
        canvasHeight = 200.dp,
        contentMaxWidth = Dp.Unspecified,
        speakerButtonSize = 64.dp,
        letterCardSize = 36.dp,
        progressBarHeight = 8.dp,
        fabSize = 56.dp,
        // Keeps the familiar three columns on a 360dp phone.
        avatarCellMinSize = 100.dp,
        avatarHeroSize = 130.dp,
    )

    private val medium = AdaptiveDimens(
        screenPadding = 24.dp,
        cardPadding = 16.dp,
        buttonHeight = 52.dp,
        starSize = 28.dp,
        canvasHeight = 280.dp,
        contentMaxWidth = 520.dp,
        speakerButtonSize = 80.dp,
        letterCardSize = 44.dp,
        progressBarHeight = 10.dp,
        fabSize = 64.dp,
        avatarCellMinSize = 130.dp,
        avatarHeroSize = 180.dp,
    )

    private val expanded = AdaptiveDimens(
        screenPadding = 32.dp,
        cardPadding = 20.dp,
        buttonHeight = 56.dp,
        starSize = 32.dp,
        canvasHeight = 400.dp,
        contentMaxWidth = Dp.Unspecified,
        speakerButtonSize = 80.dp,
        letterCardSize = 48.dp,
        progressBarHeight = 12.dp,
        fabSize = 64.dp,
        avatarCellMinSize = 160.dp,
        avatarHeroSize = 220.dp,
    )

    /** Returns the [AdaptiveDimens] for [layoutType]. */
    fun forLayout(layoutType: LayoutType): AdaptiveDimens = when (layoutType) {
        LayoutType.COMPACT -> compact
        LayoutType.MEDIUM -> medium
        LayoutType.EXPANDED -> expanded
    }

    /**
     * Returns the [AdaptiveDimens] matching the current [LocalLayoutType].
     */
    @Composable
    fun current(): AdaptiveDimens = forLayout(LocalLayoutType.current)
}
