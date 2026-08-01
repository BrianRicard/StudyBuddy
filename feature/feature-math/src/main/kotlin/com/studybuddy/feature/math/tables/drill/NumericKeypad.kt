package com.studybuddy.feature.math.tables.drill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.R as CoreUiR

/**
 * A big-button 0–9 pad. Deliberately not the system keyboard: it keeps the
 * prompt visible, cannot produce letters, and gives small fingers targets
 * well above the 48dp minimum.
 */
@Composable
fun NumericKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KEY_ROWS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { digit ->
                    DigitKey(
                        digit = digit,
                        onClick = { onDigit(digit) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Keeps 0 centred under the 8 key.
            Spacer(Modifier.weight(1f))
            DigitKey(
                digit = 0,
                onClick = { onDigit(0) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onBackspace,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(KEY_HEIGHT),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(CoreUiR.string.tables_drill_backspace),
                )
            }
        }
    }
}

@Composable
private fun DigitKey(
    digit: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(KEY_HEIGHT),
    ) {
        Text(text = digit.toString(), style = MaterialTheme.typography.headlineSmall)
    }
}

private val KEY_ROWS = listOf(
    listOf(1, 2, 3),
    listOf(4, 5, 6),
    listOf(7, 8, 9),
)

private val KEY_HEIGHT = 60.dp
