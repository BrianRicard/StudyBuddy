package com.studybuddy.core.ui.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import com.studybuddy.core.ui.animation.isReducedMotionEnabled

fun Modifier.bounceClick(onClick: () -> Unit): Modifier = composed {
    val reducedMotion = isReducedMotionEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "bounceScale",
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick,
        )
}

/**
 * Returns an animated scale value that bounces on press.
 * Use this for Cards that already have their own onClick handling.
 */
@Composable
fun rememberBounceScale(interactionSource: MutableInteractionSource): Float {
    val reducedMotion = isReducedMotionEnabled()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "bounceCardScale",
    )
    return scale
}
