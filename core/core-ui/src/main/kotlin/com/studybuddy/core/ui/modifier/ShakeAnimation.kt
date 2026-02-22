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
