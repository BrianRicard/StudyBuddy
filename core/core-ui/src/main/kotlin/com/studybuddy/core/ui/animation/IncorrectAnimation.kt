package com.studybuddy.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IncorrectAnimation(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            repeat(3) {
                shakeOffset.animateTo(10f, animationSpec = spring(stiffness = 5000f))
                shakeOffset.animateTo(-10f, animationSpec = spring(stiffness = 5000f))
            }
            shakeOffset.animateTo(0f, animationSpec = spring())
        }
    }

    Box(modifier = modifier.offset(x = shakeOffset.value.dp)) {
        content()
    }
}
