package com.studybuddy.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.theme.StreakOrange

@Composable
fun StreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = when {
            streak >= 20 -> StreakOrange
            streak >= 10 -> StreakOrange.copy(alpha = 0.8f)
            streak >= 5 -> StreakOrange.copy(alpha = 0.6f)
            else -> StreakOrange.copy(alpha = 0.4f)
        },
        label = "streakColor",
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "\uD83D\uDD25", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
