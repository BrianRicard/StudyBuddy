package com.studybuddy.feature.math.tables.drill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.mathfacts.MathFact
import com.studybuddy.core.domain.model.mathfacts.TablesCard
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.CelebrationOverlay
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.praiseRes
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun TablesDrillScreen(
    onNavigateBack: () -> Unit,
    viewModel: TablesDrillViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TablesDrillContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablesDrillContent(
    state: TablesDrillState,
    onIntent: (TablesDrillIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (state.phase == TablesDrillPhase.DRILLING) {
                        Text("${(state.index + 1).coerceAtMost(state.total)} / ${state.total}")
                    } else {
                        Text(stringResource(CoreUiR.string.tables_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (state.phase == TablesDrillPhase.DRILLING && state.combo > 1) {
                        Text(
                            text = stringResource(CoreUiR.string.drill_combo, state.combo),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .alpha(if (state.comboPaused) DIMMED_ALPHA else 1f),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            TablesDrillPhase.LOADING -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            TablesDrillPhase.DRILLING -> DrillBody(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )

            TablesDrillPhase.RESULTS -> ResultsBody(
                state = state,
                onIntent = onIntent,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DrillBody(
    state: TablesDrillState,
    onIntent: (TablesDrillIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.currentCard ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PromptCard(card = card, state = state, onIntent = onIntent)
        Spacer(Modifier.height(12.dp))
        FeedbackArea(state = state)
        Spacer(Modifier.height(12.dp))

        if (state.isResolved) {
            Button(
                onClick = { onIntent(TablesDrillIntent.Next) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.drill_next)) }
        } else {
            NumericKeypad(
                onDigit = { onIntent(TablesDrillIntent.Digit(it)) },
                onBackspace = { onIntent(TablesDrillIntent.Backspace) },
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onIntent(TablesDrillIntent.Submit) },
                enabled = state.input.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.drill_submit)) }
        }
    }
}

@Composable
private fun PromptCard(
    card: TablesCard,
    state: TablesDrillState,
    onIntent: (TablesDrillIntent) -> Unit,
) {
    StudyBuddyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${card.fact.prompt} =",
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(Modifier.size(12.dp))
                AnswerSlot(input = state.input, isResolved = state.isResolved)
            }
            IconButton(
                onClick = { onIntent(TablesDrillIntent.Replay) },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(CoreUiR.string.drill_replay),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AnswerSlot(
    input: String,
    isResolved: Boolean,
) {
    Text(
        text = input.ifBlank { "?" },
        style = MaterialTheme.typography.displaySmall,
        color = if (isResolved) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        },
    )
}

@Composable
private fun FeedbackArea(state: TablesDrillState) {
    when (val feedback = state.feedback) {
        TablesFeedback.Idle -> Unit

        TablesFeedback.Nudge -> FeedbackText(
            text = stringResource(CoreUiR.string.tables_drill_nudge),
            color = MaterialTheme.colorScheme.secondary,
        )

        is TablesFeedback.Strategy -> FeedbackText(
            text = stringResource(
                CoreUiR.string.tables_drill_strategy,
                feedback.neighbor.prompt,
                feedback.neighborProduct,
                state.currentCard?.fact?.prompt.orEmpty(),
            ),
            color = MaterialTheme.colorScheme.secondary,
        )

        is TablesFeedback.Copy -> FeedbackText(
            text = stringResource(CoreUiR.string.tables_drill_copy, feedback.answer),
            color = MaterialTheme.colorScheme.secondary,
        )

        is TablesFeedback.Correct -> FeedbackText(
            text = stringResource(praiseRes(feedback.praiseSeed)) +
                " +${feedback.pointsEarned}",
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun FeedbackText(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultsBody(
    state: TablesDrillState,
    onIntent: (TablesDrillIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(CoreUiR.string.tables_drill_done),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(CoreUiR.string.tables_drill_points, state.sessionPoints),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(
                    CoreUiR.string.tables_drill_first_try,
                    state.firstTryCount,
                    state.total,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.growths.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(CoreUiR.string.tables_drill_grew),
                    style = MaterialTheme.typography.titleMedium,
                )
                state.growths.forEach { growth ->
                    Text(
                        text = "${growth.fact.prompt} ${boxEmoji(growth.fromBox)} → ${boxEmoji(growth.toBox)}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onIntent(TablesDrillIntent.PlayAgain) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.tables_drill_again)) }
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.tables_drill_back_to_garden)) }
        }

        CelebrationOverlay(visible = true)
    }
}

private fun boxEmoji(box: Int): String = when {
    box <= 0 -> "🌱"
    box == 1 -> "🌿"
    box <= 3 -> "🌸"
    else -> "🌳"
}

private const val DIMMED_ALPHA = 0.4f

@Preview(showBackground = true)
@Composable
private fun TablesDrillPreview() {
    StudyBuddyTheme {
        TablesDrillContent(
            state = TablesDrillState(
                phase = TablesDrillPhase.DRILLING,
                cards = listOf(TablesCard(fact = MathFact(7, 8), box = 1, isNew = false)),
                input = "5",
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
