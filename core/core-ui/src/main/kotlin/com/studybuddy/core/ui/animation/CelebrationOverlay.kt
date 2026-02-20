package com.studybuddy.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    if (!visible) return

    val alpha = remember { Animatable(1f) }

    LaunchedEffect(visible) {
        alpha.snapTo(1f)
        kotlinx.coroutines.delay(1500)
        alpha.animateTo(0f, animationSpec = tween(500))
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value),
    ) {
        // TODO: Replace with Lottie confetti animation
    }
}
