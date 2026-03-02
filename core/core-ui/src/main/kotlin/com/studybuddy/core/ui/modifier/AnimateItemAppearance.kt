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
 * Staggered fade-in + slide-up appearance animation for list items.
 * Each item gets a 50ms stagger delay based on its [index].
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

    LaunchedEffect(Unit) {
        val delay = index * staggerMs
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, delayMillis = delay),
        )
    }

    LaunchedEffect(Unit) {
        val delay = index * staggerMs
        translationY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = durationMs, delayMillis = delay),
        )
    }

    this
        .alpha(alpha.value)
        .graphicsLayer { this.translationY = translationY.value }
}
