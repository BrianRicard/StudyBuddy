package com.studybuddy.feature.conjugation.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.CorrectAnswerAnimation
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.conjugation.components.StepProgressBar
import com.studybuddy.feature.conjugation.components.praiseRes

@Composable
fun WriteScreen(
    onNavigateBack: () -> Unit,
    viewModel: WriteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WriteEffect.Completed -> onNavigateBack()
            }
        }
    }

    WriteContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteContent(
    state: WriteState,
    onIntent: (WriteIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.conjugation_write_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val stage = state.stage ?: return@Scaffold
        val isCorrect = state.feedback is WriteFeedback.Correct

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepProgressBar(
                current = state.index + if (isCorrect) 1 else 0,
                total = state.total,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(CoreUiR.string.conjugation_write_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stage.verb.infinitive,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))

            CorrectAnswerAnimation(isCorrect = isCorrect) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.person.pronoun,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = { onIntent(WriteIntent.InputChanged(it)) },
                        label = { Text(stringResource(CoreUiR.string.conjugation_answer_field)) },
                        singleLine = true,
                        enabled = !isCorrect,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onIntent(WriteIntent.Submit) }),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            FeedbackBanner(state = state, onIntent = onIntent)
            Spacer(Modifier.height(24.dp))

            if (isCorrect) {
                StudyBuddyButton(
                    text = stringResource(CoreUiR.string.conjugation_next),
                    onClick = { onIntent(WriteIntent.Next) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                )
            } else {
                StudyBuddyButton(
                    text = stringResource(CoreUiR.string.conjugation_write_check),
                    onClick = { onIntent(WriteIntent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.input.isNotBlank(),
                )
            }
        }
    }
}

@Composable
private fun FeedbackBanner(
    state: WriteState,
    onIntent: (WriteIntent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (val feedback = state.feedback) {
            is WriteFeedback.Idle -> Unit

            is WriteFeedback.Correct -> Text(
                text = stringResource(praiseRes(feedback.praiseSeed)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )

            is WriteFeedback.TryAgain -> {
                Text(
                    text = stringResource(CoreUiR.string.conjugation_write_try_again),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.canRevealHint) {
                    TextButton(onClick = { onIntent(WriteIntent.RevealHint) }) {
                        Text(stringResource(CoreUiR.string.conjugation_write_hint_button))
                    }
                }
            }

            is WriteFeedback.Revealed -> Text(
                text = stringResource(CoreUiR.string.conjugation_write_reveal, feedback.correctForm),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WritePreview() {
    StudyBuddyTheme {
        WriteContent(
            state = WriteState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
