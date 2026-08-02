package com.studybuddy.core.ui.adaptive

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The avatar grids size themselves from [AdaptiveDimens.avatarCellMinSize] and a
 * fraction of the resulting cell. Both halves of that arithmetic have produced
 * user-visible bugs, so they are pinned here rather than left to a screenshot.
 */
class AvatarGridSizingTest {

    /**
     * Mirrors `GridCells.Adaptive`: it keeps cells near `minSize` and adds
     * columns, so the column count — not the cell width — is what grows.
     */
    private fun columnCount(
        windowWidth: Int,
        chrome: Int,
        minSize: Int,
    ): Int {
        val available = windowWidth - chrome - CONTENT_PADDING * 2
        return maxOf((available + SPACING) / (minSize + SPACING), 1)
    }

    private fun cellWidth(
        windowWidth: Int,
        chrome: Int,
        minSize: Int,
    ): Float {
        val available = windowWidth - chrome - CONTENT_PADDING * 2
        val columns = columnCount(windowWidth, chrome, minSize)
        return (available - SPACING * (columns - 1)).toFloat() / columns
    }

    private fun compactMin() = AdaptiveDimensDefaults.forLayout(LayoutType.COMPACT).avatarCellMinSize.value.toInt()

    @Test
    fun `a phone keeps its three columns`() {
        assertEquals(3, columnCount(windowWidth = 360, chrome = 0, minSize = compactMin()))
        assertEquals(3, columnCount(windowWidth = 411, chrome = 0, minSize = compactMin()))
    }

    @Test
    fun `a tablet gets bigger cells, not more phone-sized ones`() {
        // 800dp portrait behind an 80dp navigation rail, 1280dp landscape behind
        // a 240dp drawer — the two shapes the app actually runs in.
        val phoneCell = cellWidth(windowWidth = 411, chrome = 0, minSize = compactMin())
        val mediumCell = cellWidth(
            windowWidth = 800,
            chrome = 80,
            minSize = AdaptiveDimensDefaults.forLayout(LayoutType.MEDIUM).avatarCellMinSize.value.toInt(),
        )
        val expandedCell = cellWidth(
            windowWidth = 1280,
            chrome = 240,
            minSize = AdaptiveDimensDefaults.forLayout(LayoutType.EXPANDED).avatarCellMinSize.value.toInt(),
        )

        assertTrue(mediumCell > phoneCell, "medium cell $mediumCell should beat phone cell $phoneCell")
        assertTrue(expandedCell > mediumCell, "expanded cell $expandedCell should beat medium cell $mediumCell")
    }

    /**
     * Mirrors `CharacterCard`: art is a share of the width, capped by the height
     * left once the name and the badge band are paid for. Without the cap a long
     * name is drawn on top of the price on every phone-sized cell.
     */
    private fun artSize(
        cellWidth: Float,
        labelHeight: Float,
    ): Float {
        val cellHeight = cellWidth / CARD_ASPECT
        val available = cellHeight - labelHeight - CARD_CHROME
        return available.coerceIn(0f, cellWidth * CHARACTER_SIZE_FRACTION)
    }

    @Test
    fun `art plus name plus badges always fit inside the card`() {
        val cells = listOf(88f, 101f, 118f, 138f, 163f, 192f, 328f)
        val labelHeights = listOf(16f, 20.8f, 32f) // font scale 1.0, 1.3, 2.0

        for (cell in cells) {
            for (label in labelHeights) {
                val used = artSize(cell, label) + label + CARD_CHROME
                val cellHeight = cell / CARD_ASPECT
                assertTrue(
                    used <= cellHeight + TOLERANCE,
                    "cell ${cell}dp at label ${label}dp needs ${used}dp of ${cellHeight}dp",
                )
            }
        }
    }

    @Test
    fun `the cap only bites on small cells, so tablets keep the full art`() {
        // A phone cell is height-bound; a tablet cell is width-bound.
        assertTrue(artSize(cellWidth = 101f, labelHeight = 16f) < 101f * CHARACTER_SIZE_FRACTION)
        assertEquals(163f * CHARACTER_SIZE_FRACTION, artSize(cellWidth = 163f, labelHeight = 16f), TOLERANCE)
    }

    @Test
    fun `art never shrinks below what the fixed-size version showed`() {
        // The closet used a flat 48.dp before this became responsive; going
        // backwards on a phone would be a regression, not a fix.
        assertTrue(artSize(cellWidth = 101f, labelHeight = 16f) > 48f)
        assertTrue(artSize(cellWidth = 118f, labelHeight = 16f) > 48f)
    }

    @Test
    fun `the hero avatar is capped against short screens`() {
        val expandedHero = AdaptiveDimensDefaults.forLayout(LayoutType.EXPANDED).avatarHeroSize
        // A phone in landscape is EXPANDED but only ~411dp tall.
        val landscapeHero = minOf(expandedHero, 411.dp * HERO_MAX_SCREEN_FRACTION)

        assertTrue(landscapeHero < expandedHero, "hero must shrink on a short screen")
        assertTrue(
            landscapeHero <= 411.dp * HERO_MAX_SCREEN_FRACTION,
            "hero must leave the grid most of the screen",
        )
    }

    private companion object {
        const val CONTENT_PADDING = 16
        const val SPACING = 12
        const val CARD_ASPECT = 0.85f
        const val CHARACTER_SIZE_FRACTION = 0.68f
        const val HERO_MAX_SCREEN_FRACTION = 0.35f

        /** Card padding (8 top + 8 bottom), the 4dp gap, and the 24dp badge band. */
        const val CARD_CHROME = 44f
        const val TOLERANCE = 0.01f
    }
}
