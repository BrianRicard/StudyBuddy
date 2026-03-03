package com.studybuddy.core.ui.adaptive

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A two-pane side-by-side layout used on EXPANDED screens.
 *
 * @param leftWeight Weight fraction for the left pane (0.0–1.0)
 * @param modifier Modifier for the outer Row
 * @param leftPane Content for the left pane
 * @param rightPane Content for the right pane
 */
@Composable
fun TwoPaneLayout(
    leftWeight: Float,
    modifier: Modifier = Modifier,
    leftPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(leftWeight).fillMaxHeight()) {
            leftPane()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(modifier = Modifier.weight(1f - leftWeight).fillMaxHeight()) {
            rightPane()
        }
    }
}
