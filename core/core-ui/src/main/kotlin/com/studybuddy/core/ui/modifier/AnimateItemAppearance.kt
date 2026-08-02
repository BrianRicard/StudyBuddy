package com.studybuddy.core.ui.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Staggered fade-in + slide-up appearance animation for list items, so a screen
 * assembles itself in a gentle cascade rather than snapping into place.
 *
 * The stagger is capped at [MAX_STAGGER_MS]. In a lazy list or grid an item is
 * composed only when it scrolls into view, so its [index] says nothing about
 * how long it has been waiting: uncapped, the 60th character in the avatar
 * closet sat completely invisible for three seconds after being scrolled to,
 * and it grew worse further down. Capping keeps the cascade over roughly the
 * first screenful and lets everything after it arrive promptly.
 *
 * Respects reduced motion — when animations are disabled, items appear instantly.
 */
fun Modifier.animateItemAppearance(
    index: Int,
    durationMs: Int = 300,
    staggerMs: Int = 50,
): Modifier = composed {
    val reducedMotion = com.studybuddy.core.ui.animation.isReducedMotionEnabled()

    if (reducedMotion) return@composed this

    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(24f) }
    val delay = (index * staggerMs).coerceIn(0, MAX_STAGGER_MS)

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, delayMillis = delay),
        )
    }

    LaunchedEffect(Unit) {
        translationY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = durationMs, delayMillis = delay),
        )
    }

    this
        .alpha(alpha.value)
        .graphicsLayer { this.translationY = translationY.value }
}

/**
 * Longest an item may wait before it starts appearing. At the default 50ms
 * stagger this cascades the first seven items and then stops accumulating —
 * comfortably a screenful, and never long enough to read as "loading".
 */
private const val MAX_STAGGER_MS = 300
