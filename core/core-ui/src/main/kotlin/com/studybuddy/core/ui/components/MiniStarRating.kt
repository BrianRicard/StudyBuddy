package com.studybuddy.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val StarAmber = Color(0xFFFFC107)
private val StarEmpty = Color(0xFFE0E0E0)

/**
 * Compact star display for list cards.
 * Shows filled/empty stars based on the average rating.
 */
@Composable
fun MiniStarRating(
    stars: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 14.dp,
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= stars) StarAmber else StarEmpty,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
