package com.studybuddy.core.ui.animation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreakFireAnimation(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streakFire")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            streak >= 20 -> 1.3f
            streak >= 10 -> 1.2f
            streak >= 5 -> 1.1f
            else -> 1.05f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fireScale",
    )

    Text(
        text = "\uD83D\uDD25",
        fontSize = 24.sp,
        modifier = modifier.scale(scale),
    )
}
