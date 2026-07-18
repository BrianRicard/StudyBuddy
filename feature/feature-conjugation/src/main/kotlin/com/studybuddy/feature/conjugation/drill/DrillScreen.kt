package com.studybuddy.feature.conjugation.drill

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.conjugation.AtelierSchedule
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.TimeoutAmber
import com.studybuddy.feature.conjugation.components.praiseRes
import com.studybuddy.shared.ink.HandwritingCanvas

private val ACCENTS = listOf("é", "è", "ê", "ë", "à", "â", "ç", "î", "ï", "ô", "û", "ù")

@Composable
fun DrillScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrillViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DrillContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrillContent(
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (state.phase == DrillPhase.DRILLING) {
                        Text("${(state.index + 1).coerceAtMost(state.total)} / ${state.total}")
                    } else {
                        Text(stringResource(CoreUiR.string.atelier_title))
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
                    if (state.phase == DrillPhase.DRILLING && state.combo > 1) {
                        Text(
                            text = stringResource(CoreUiR.string.drill_combo, state.combo),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .alphaIf(state.comboPaused),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            DrillPhase.LOADING -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            DrillPhase.DRILLING -> DrillBody(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )

            DrillPhase.RESULTS -> ResultsBody(
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
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.currentCard ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StudyBuddyCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(CoreUiR.string.drill_prompt_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(card.tense.displayName) },
                )
                IconButton(
                    onClick = { onIntent(DrillIntent.Replay) },
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(CoreUiR.string.drill_replay),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        InputModeTabs(state = state, onIntent = onIntent)
        Spacer(Modifier.height(8.dp))

        when (state.inputMode) {
            DrillInputMode.KEYBOARD -> KeyboardInput(state = state, onIntent = onIntent)
            DrillInputMode.STYLUS -> StylusInput(state = state, onIntent = onIntent)
        }

        Spacer(Modifier.height(12.dp))
        FeedbackArea(state = state)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(12.dp))

        if (state.isResolved) {
            Button(
                onClick = { onIntent(DrillIntent.Next) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.drill_next)) }
        } else {
            Button(
                onClick = { onIntent(DrillIntent.Submit) },
                enabled = state.input.isNotBlank() && !state.isRecognizingInk,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(CoreUiR.string.drill_submit)) }
        }
    }
}

@Composable
private fun InputModeTabs(
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = state.inputMode == DrillInputMode.KEYBOARD,
                onClick = { onIntent(DrillIntent.SetInputMode(DrillInputMode.KEYBOARD)) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(stringResource(CoreUiR.string.drill_tab_keyboard)) }
            SegmentedButton(
                selected = state.inputMode == DrillInputMode.STYLUS,
                onClick = { onIntent(DrillIntent.SetInputMode(DrillInputMode.STYLUS)) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(stringResource(CoreUiR.string.drill_tab_stylus)) }
        }
        Text(
            text = stringResource(
                CoreUiR.string.drill_stylus_bonus_note,
                PointValues.CONJUGATION_DRILL_STYLUS_BONUS,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun KeyboardInput(
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(state.input)) }

    // Keep the local cursor-aware value in sync when the ViewModel changes the
    // input from outside (next card, reveal, ink recognition).
    LaunchedEffect(state.input) {
        if (fieldValue.text != state.input) {
            fieldValue = TextFieldValue(state.input, selection = TextRange(state.input.length))
        }
    }

    Column {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onIntent(DrillIntent.InputChanged(it.text))
            },
            enabled = !state.isResolved,
            placeholder = { Text(stringResource(CoreUiR.string.drill_input_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onIntent(DrillIntent.Submit) }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        // Accent bar: accents one tap away, so no attention is wasted hunting
        // for them in keyboard long-press menus.
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            ACCENTS.forEach { accent ->
                AssistChip(
                    onClick = {
                        val selection = fieldValue.selection
                        val newText = fieldValue.text.replaceRange(
                            selection.start,
                            selection.end,
                            accent,
                        )
                        fieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(selection.start + accent.length),
                        )
                        onIntent(DrillIntent.InputChanged(newText))
                    },
                    enabled = !state.isResolved,
                    label = { Text(accent, style = MaterialTheme.typography.titleMedium) },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun StylusInput(
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
) {
    Column {
        HandwritingCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            recognizedText = state.input,
            referenceWord = state.currentCard?.prompt.orEmpty(),
            onInkReady = { ink -> onIntent(DrillIntent.RecognizeInk(ink)) },
            onClear = { onIntent(DrillIntent.InputChanged("")) },
            onUndo = { ink ->
                if (ink != null) {
                    onIntent(DrillIntent.RecognizeInk(ink))
                } else {
                    onIntent(DrillIntent.InputChanged(""))
                }
            },
        )
        if (state.inkFailed) {
            Text(
                text = stringResource(CoreUiR.string.drill_ink_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = TimeoutAmber,
            )
        }
    }
}

@Composable
private fun FeedbackArea(state: DrillState) {
    when (val feedback = state.feedback) {
        DrillFeedback.Idle -> Unit

        DrillFeedback.AccentGlow -> FeedbackText(stringResource(CoreUiR.string.drill_accent_glow))
        DrillFeedback.ElisionGlow -> FeedbackText(stringResource(CoreUiR.string.drill_elision_glow))
        DrillFeedback.ListenAgain -> FeedbackText(stringResource(CoreUiR.string.drill_listen_again))

        is DrillFeedback.Locate -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FeedbackText(stringResource(CoreUiR.string.drill_locate))
            Text(
                text = locateAnnotated(state.input, feedback.matchedPrefixLength),
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        is DrillFeedback.Hint -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FeedbackText(stringResource(CoreUiR.string.drill_hint))
            Text(
                text = feedback.skeleton,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        is DrillFeedback.Copy -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = feedback.correct,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            FeedbackText(stringResource(CoreUiR.string.drill_copy_prompt))
        }

        is DrillFeedback.Correct -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(praiseRes(feedback.praiseSeed)),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(CoreUiR.string.drill_points_chip, feedback.pointsEarned),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            feedback.twin?.let { twin ->
                Text(
                    text = stringResource(CoreUiR.string.drill_twin_note, twin),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeedbackText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ResultsBody(
    state: DrillState,
    onIntent: (DrillIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎉", style = MaterialTheme.typography.displayLarge)
        Text(
            text = stringResource(CoreUiR.string.drill_results_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(CoreUiR.string.drill_results_stars, state.sessionPoints),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(
                CoreUiR.string.drill_results_first_try,
                state.firstTryCount,
                state.resolvedCount,
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.growths.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(CoreUiR.string.drill_results_growth_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            state.growths.forEach { growth ->
                Text(
                    text = "${growth.verbInfinitive} · ${growth.tense.displayName}  " +
                        "${boxEmoji(growth.fromBox)} → ${boxEmoji(growth.toBox)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onIntent(DrillIntent.PlayAgain) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(CoreUiR.string.drill_play_again)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(CoreUiR.string.drill_done)) }
    }
}

/** The child's answer with the trusted prefix green and the rest amber — locate, don't tell. */
private fun locateAnnotated(
    input: String,
    matchedPrefixLength: Int,
): AnnotatedString {
    val split = matchedPrefixLength.coerceIn(0, input.length)
    return buildAnnotatedString {
        withStyle(SpanStyle(color = LocateOkColor)) { append(input.take(split)) }
        withStyle(SpanStyle(color = TimeoutAmber, fontWeight = FontWeight.Bold)) {
            append(input.drop(split))
        }
    }
}

private fun boxEmoji(box: Int): String = when {
    box >= AtelierSchedule.MAX_BOX -> "🌳"
    box >= 3 -> "🌸"
    box >= 2 -> "🌿"
    else -> "🌱"
}

/** Dims the combo chip while a stumble has it paused — paused, never reset. */
private fun Modifier.alphaIf(dimmed: Boolean): Modifier = if (dimmed) alpha(DIMMED_ALPHA) else this

private const val DIMMED_ALPHA = 0.4f
private val LocateOkColor = Color(0xFF2E7D32)

@Preview(showBackground = true)
@Composable
private fun DrillScreenPreview() {
    StudyBuddyTheme {
        DrillContent(
            state = DrillState(phase = DrillPhase.LOADING),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
