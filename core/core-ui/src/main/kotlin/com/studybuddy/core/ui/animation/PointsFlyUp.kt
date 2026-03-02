package com.studybuddy.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.theme.PointsGold

@Composable
fun PointsFlyUp(
    points: Int,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {},
) {
    val reducedMotion = isReducedMotionEnabled()
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(points) {
        if (reducedMotion) {
            kotlinx.coroutines.delay(800)
            onAnimationEnd()
        } else {
            offsetY.animateTo(-80f, animationSpec = tween(durationMillis = 800))
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
            onAnimationEnd()
        }
    }

    Text(
        text = "+$points",
        style = MaterialTheme.typography.headlineSmall,
        color = PointsGold,
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value),
    )
}
