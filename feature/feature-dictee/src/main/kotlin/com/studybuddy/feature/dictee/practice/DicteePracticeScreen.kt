package com.studybuddy.feature.dictee.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.ui.animation.CelebrationOverlay
import com.studybuddy.core.ui.animation.CorrectAnswerAnimation
import com.studybuddy.core.ui.animation.IncorrectAnimation
import com.studybuddy.core.ui.animation.PointsFlyUp
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StreakIndicator
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.components.StudyBuddyProgressBar
import com.studybuddy.shared.ink.HandwritingCanvas

@Composable
fun DicteePracticeScreen(
    onNavigateBack: () -> Unit,
    viewModel: DicteePracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DicteePracticeEffect.ShowPoints -> { /* Handled by animation in UI */ }
                is DicteePracticeEffect.NavigateToResults -> onNavigateBack()
            }
        }
    }

    DicteePracticeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteePracticeContent(
    state: DicteePracticeState,
    onIntent: (DicteePracticeIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.listTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    PointsBadge(points = state.sessionScore)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (state.streak > 0) {
                        StreakIndicator(streak = state.streak)
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isComplete) {
                PracticeCompleteContent(
                    state = state,
                    onNavigateBack = onNavigateBack,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Progress bar
                    StudyBuddyProgressBar(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${state.currentIndex + 1} / ${state.totalWords}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TTS controls
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StudyBuddyButton(
                            text = if (state.isPlaying) "Playing..." else "\uD83D\uDD0A Play",
                            onClick = { onIntent(DicteePracticeIntent.PlayWord) },
                            enabled = !state.isPlaying,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StudyBuddyOutlinedButton(
                            text = "Slow",
                            onClick = { onIntent(DicteePracticeIntent.PlayWordSlow) },
                            enabled = !state.isPlaying,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hint
                    AnimatedVisibility(visible = state.hintVisible) {
                        val word = state.currentWord
                        if (word != null) {
                            Text(
                                text = "${word.word.first()}${"_".repeat(word.word.length - 1)} (${word.word.length} letters)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (!state.hintVisible && state.feedback == null) {
                        TextButton(onClick = { onIntent(DicteePracticeIntent.ShowHint) }) {
                            Text("Show Hint")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input mode toggle
                    Row {
                        TextButton(
                            onClick = {
                                if (state.inputMode != InputMode.KEYBOARD) {
                                    onIntent(DicteePracticeIntent.ToggleInputMode)
                                }
                            },
                        ) {
                            Text(
                                text = "Keyboard",
                                color = if (state.inputMode == InputMode.KEYBOARD) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        TextButton(
                            onClick = {
                                if (state.inputMode != InputMode.HANDWRITING) {
                                    onIntent(DicteePracticeIntent.ToggleInputMode)
                                }
                            },
                        ) {
                            Text(
                                text = "Handwriting",
                                color = if (state.inputMode == InputMode.HANDWRITING) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input area
                    when (state.inputMode) {
                        InputMode.KEYBOARD -> {
                            OutlinedTextField(
                                value = state.userInput,
                                onValueChange = { onIntent(DicteePracticeIntent.UpdateInput(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Type the word") },
                                singleLine = true,
                                enabled = state.feedback == null,
                            )
                        }
                        InputMode.HANDWRITING -> {
                            HandwritingCanvas(
                                modifier = Modifier.fillMaxWidth(),
                                onInkReady = { ink ->
                                    // Recognition is handled by the ViewModel
                                },
                                onClear = { onIntent(DicteePracticeIntent.UpdateInput("")) },
                            )
                            state.recognizedText?.let { text ->
                                Text(
                                    text = "Recognized: $text",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback area
                    when (val feedback = state.feedback) {
                        is Feedback.Correct -> {
                            CorrectAnswerAnimation(visible = true) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Correct!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Great job! Keep going!",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            StudyBuddyButton(
                                text = "Next Word",
                                onClick = { onIntent(DicteePracticeIntent.NextWord) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        is Feedback.Incorrect -> {
                            IncorrectAnimation(visible = true) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Almost there!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "The correct spelling is:",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = feedback.correctAnswer,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StudyBuddyOutlinedButton(
                                    text = "Try Again",
                                    onClick = { onIntent(DicteePracticeIntent.RetryWord) },
                                    modifier = Modifier.weight(1f),
                                )
                                StudyBuddyButton(
                                    text = "Next Word",
                                    onClick = { onIntent(DicteePracticeIntent.NextWord) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        null -> {
                            StudyBuddyButton(
                                text = "Check",
                                onClick = { onIntent(DicteePracticeIntent.CheckAnswer) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.userInput.isNotBlank(),
                            )
                        }
                        else -> { /* TimeUp not used in dictée */ }
                    }
                }
            }

            // Celebration overlay for correct answers
            AnimatedVisibility(
                visible = state.feedback is Feedback.Correct,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CelebrationOverlay(
                    visible = true,
                    onDismiss = {},
                )
            }

            // Points fly-up animation
            AnimatedVisibility(
                visible = state.feedback is Feedback.Correct,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PointsFlyUp(
                        points = when (state.inputMode) {
                            InputMode.KEYBOARD -> 10
                            InputMode.HANDWRITING -> 15
                        },
                        visible = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeCompleteContent(
    state: DicteePracticeState,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Practice Complete!",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You scored ${state.sessionScore} points",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "on \"${state.listTitle}\"",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        StudyBuddyButton(
            text = "Done",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun DicteePracticeScreenPreview() {
    DicteePracticeContent(
        state = DicteePracticeState(
            listTitle = "Les Animaux",
            words = listOf(
                DicteeWord(id = "1", listId = "l", word = "maison"),
                DicteeWord(id = "2", listId = "l", word = "chat"),
            ),
            currentIndex = 0,
            userInput = "mais",
            sessionScore = 25,
            streak = 2,
        ),
        onIntent = {},
        onNavigateBack = {},
    )
}
