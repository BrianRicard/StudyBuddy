package com.studybuddy.core.ui.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            repeat(3) {
                translationX.animateTo(8f, animationSpec = spring(stiffness = 5000f))
                translationX.animateTo(-8f, animationSpec = spring(stiffness = 5000f))
            }
            translationX.animateTo(0f, animationSpec = spring())
        }
    }

    graphicsLayer { this.translationX = translationX.value }
}

/**
 * Event-keyed variant: shakes whenever [eventKey] changes while [active] is
 * true, so repeated triggers on the same element (same boolean state) still
 * animate every time.
 */
fun Modifier.shake(
    eventKey: Int,
    active: Boolean,
): Modifier = composed {
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(eventKey, active) {
        if (active && eventKey > 0) {
            repeat(3) {
                translationX.animateTo(8f, animationSpec = spring(stiffness = 5000f))
                translationX.animateTo(-8f, animationSpec = spring(stiffness = 5000f))
            }
            translationX.animateTo(0f, animationSpec = spring())
        }
    }

    graphicsLayer { this.translationX = translationX.value }
}
