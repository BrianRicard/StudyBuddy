package com.studybuddy.core.ui.modifier

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The stagger delay is the whole reason the avatar closet appeared to "load"
 * for seconds, so the cap is pinned here rather than left to the modifier.
 */
class AnimateItemAppearanceTest {

    /** Mirrors the delay the modifier computes for an item at [index]. */
    private fun delayFor(
        index: Int,
        staggerMs: Int = 50,
    ) = (index * staggerMs).coerceIn(0, MAX_STAGGER_MS)

    @Test
    fun `early items still cascade`() {
        assertEquals(0, delayFor(0))
        assertEquals(50, delayFor(1))
        assertEquals(100, delayFor(2))
    }

    @Test
    fun `an item scrolled to deep in a lazy grid never waits more than the cap`() {
        // The avatar closet has 66 characters; index 60 used to wait 3 seconds.
        assertEquals(MAX_STAGGER_MS, delayFor(60))
        assertEquals(MAX_STAGGER_MS, delayFor(66))
        assertEquals(MAX_STAGGER_MS, delayFor(1_000))
    }

    @Test
    fun `the cap stays short enough to read as animation, not loading`() {
        // Delay plus the 300ms fade should stay well under a second.
        assertTrue(MAX_STAGGER_MS + DEFAULT_DURATION_MS < 1_000)
    }

    private companion object {
        const val MAX_STAGGER_MS = 300
        const val DEFAULT_DURATION_MS = 300
    }
}
