package com.studybuddy.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Constrains child content to [AdaptiveDimens.contentMaxWidth] on MEDIUM layout,
 * fills available width on COMPACT and EXPANDED.
 */
@Composable
fun ContentConstraint(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dimens = AdaptiveDimensDefaults.current()
    val layoutType = LocalLayoutType.current

    if (layoutType == LayoutType.MEDIUM) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(modifier = Modifier.widthIn(max = dimens.contentMaxWidth)) {
                content()
            }
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}
