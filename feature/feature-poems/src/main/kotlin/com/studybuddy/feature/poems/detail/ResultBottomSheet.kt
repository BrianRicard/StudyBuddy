package com.studybuddy.feature.poems.detail

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.R as CoreUiR

private val CorrectBg = Color(0xFFC8E6C9)
private val IncorrectBg = Color(0xFFFFCDD2)
private val UnclearBg = Color(0xFFFFF9C4)
private const val SKIPPED_ALPHA = 0.35f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(
    score: PoemScore,
    words: List<WordInfo>,
    onTapWord: (Int) -> Unit,
    onTryAgain: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        ResultContent(
            score = score,
            words = words,
            onTapWord = onTapWord,
            onTryAgain = onTryAgain,
        )
    }
}

/**
 * The inner content of poem scoring results. Used inside [ResultBottomSheet] on COMPACT,
 * and rendered inline on EXPANDED tablet layouts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultContent(
    score: PoemScore,
    words: List<WordInfo>,
    onTapWord: (Int) -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(CoreUiR.string.poems_result_title),
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Star rating
        AnimatedStarRating(starCount = score.starRating)

        Spacer(modifier = Modifier.height(8.dp))

        // Encouragement
        Text(
            text = stringResource(score.encouragementResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Colour legend
        ColourLegend()

        Spacer(modifier = Modifier.height(16.dp))

        // All words coloured by state
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            words.forEach { word ->
                ScoredWord(
                    word = word,
                    onClick = { onTapWord(word.globalIndex) },
                )
            }
        }

        // Words to practise
        val practiceWords = words.filter {
            it.state == WordState.INCORRECT || it.state == WordState.UNCLEAR
        }
        if (practiceWords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(CoreUiR.string.poems_words_to_practise),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(CoreUiR.string.poems_tap_word_to_hear),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                practiceWords.forEach { word ->
                    ScoredWord(
                        word = word,
                        onClick = { onTapWord(word.globalIndex) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(CoreUiR.string.poems_try_again))
        }
    }
}

@Composable
private fun AnimatedStarRating(starCount: Int) {
    var animatedStars by remember { mutableStateOf(0) }

    LaunchedEffect(starCount) {
        animatedStars = starCount
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            val targetAlpha = if (i <= animatedStars) 1f else 0.3f
            val alpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = tween(durationMillis = 300, delayMillis = i * 100),
                label = "star_$i",
            )

            Icon(
                imageVector = if (i <= animatedStars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier
                    .size(40.dp)
                    .alpha(alpha),
            )
        }
    }
}

@Composable
private fun ColourLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LegendItem(color = CorrectBg, text = stringResource(CoreUiR.string.poems_legend_correct))
        LegendItem(color = IncorrectBg, text = stringResource(CoreUiR.string.poems_legend_incorrect))
        LegendItem(color = UnclearBg, text = stringResource(CoreUiR.string.poems_legend_unclear))
        LegendItem(
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = stringResource(CoreUiR.string.poems_legend_skipped),
            alpha = SKIPPED_ALPHA,
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    alpha: Float = 1f,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .alpha(alpha),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(alpha),
        )
    }
}

@Composable
private fun ScoredWord(
    word: WordInfo,
    onClick: () -> Unit,
) {
    val backgroundColor = when (word.state) {
        WordState.CORRECT -> CorrectBg
        WordState.INCORRECT -> IncorrectBg
        WordState.UNCLEAR -> UnclearBg
        WordState.UNREAD, WordState.SKIPPED -> Color.Transparent
    }

    val textDecoration = if (word.state == WordState.INCORRECT) {
        TextDecoration.Underline
    } else {
        TextDecoration.None
    }

    val wordAlpha = if (word.state == WordState.SKIPPED) SKIPPED_ALPHA else 1f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .alpha(wordAlpha),
    ) {
        Text(
            text = word.text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = textDecoration,
        )
    }
}
