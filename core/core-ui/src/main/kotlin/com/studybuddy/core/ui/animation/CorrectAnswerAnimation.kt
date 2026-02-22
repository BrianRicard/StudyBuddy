package com.studybuddy.core.ui.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.theme.CorrectGreen

@Composable
fun CorrectAnswerAnimation(
    isCorrect: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isCorrect) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.3f),
        label = "correctScale",
    )

    val bgColor by animateColorAsState(
        targetValue = if (isCorrect) {
            CorrectGreen.copy(alpha = 0.1f)
        } else {
            androidx.compose.ui.graphics.Color.Transparent
        },
        label = "correctBg",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(bgColor, RoundedCornerShape(16.dp)),
    ) {
        content()
    }
}
