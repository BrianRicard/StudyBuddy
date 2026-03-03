package com.studybuddy.feature.dictee.practice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TileCornerRadius = RoundedCornerShape(12.dp)
private val SlotCornerRadius = RoundedCornerShape(12.dp)
private val TileSize = 48.dp
private val WarmCream = Color(0xFFFFF8E1)
private val FilledSlotBg = Color(0xFFE8EAF6) // Light indigo
private val EmptySlotBorder = Color(0xFFBDBDBD)

/**
 * Letter tile input for EASY difficulty dictée.
 * Shows answer slots at top and scrambled letter tiles below.
 * Children tap tiles to fill slots and tap filled slots to return tiles.
 *
 * @param answerSlots Current state of answer slots (null = empty).
 * @param tiles Available letter tiles with their used state.
 * @param enabled Whether interaction is allowed.
 * @param onTapTile Called when a tile in the available pool is tapped.
 * @param onRemoveFromSlot Called when a filled slot is tapped to return its letter.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterTileInput(
    answerSlots: List<Char?>,
    tiles: List<LetterTile>,
    enabled: Boolean = true,
    onTapTile: (Int) -> Unit,
    onRemoveFromSlot: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Answer slots row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            answerSlots.forEachIndexed { index, letter ->
                AnswerSlot(
                    letter = letter,
                    onClick = {
                        if (enabled && letter != null) {
                            onRemoveFromSlot(index)
                        }
                    },
                )
                if (index < answerSlots.lastIndex) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Available letter tiles
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            tiles.forEachIndexed { index, tile ->
                AvailableTile(
                    letter = tile.letter,
                    isUsed = tile.isUsed,
                    onClick = {
                        if (enabled && !tile.isUsed) {
                            onTapTile(index)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AnswerSlot(
    letter: Char?,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (letter != null) FilledSlotBg else WarmCream,
        animationSpec = tween(200),
        label = "slotBg",
    )

    Box(
        modifier = Modifier
            .size(TileSize)
            .clip(SlotCornerRadius)
            .background(backgroundColor)
            .clickable(enabled = letter != null, onClick = onClick)
            .then(
                if (letter == null) {
                    Modifier.background(Color.Transparent)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != null) {
            Text(
                text = letter.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                ),
                textAlign = TextAlign.Center,
            )
        } else {
            // Empty slot — show underline
            Box(
                modifier = Modifier
                    .width(TileSize - 12.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(EmptySlotBorder)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun AvailableTile(
    letter: Char,
    isUsed: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isUsed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "tileBg",
    )

    val textAlpha = if (isUsed) 0.2f else 1f

    Box(
        modifier = Modifier
            .size(TileSize)
            .clip(TileCornerRadius)
            .background(backgroundColor)
            .clickable(enabled = !isUsed, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
            textAlign = TextAlign.Center,
        )
    }
}
