package com.studybuddy.core.ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Minimum touch target size per Material Design / WCAG guidelines (48dp).
 */
private val MinTouchTarget = 48.dp

/**
 * Modifier that ensures a minimum 48dp touch target and sets an accessible
 * content description for screen readers.
 */
fun Modifier.accessibleClickable(
    label: String,
    role: Role = Role.Button,
    onClick: () -> Unit,
): Modifier =
    this
        .defaultMinSize(minWidth = MinTouchTarget, minHeight = MinTouchTarget)
        .semantics {
            contentDescription = label
            this.role = role
        }
        .clickable(onClick = onClick)
