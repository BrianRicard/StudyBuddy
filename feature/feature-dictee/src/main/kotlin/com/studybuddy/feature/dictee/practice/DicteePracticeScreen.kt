package com.studybuddy.feature.dictee.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.CelebrationOverlay
import com.studybuddy.core.ui.animation.PointsFlyUp
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StreakIndicator
import com.studybuddy.core.ui.components.StudyBuddyProgressBar
import com.studybuddy.shared.ink.HandwritingCanvas

// Shared feedback colors — identical to poems section
private val CorrectBg = Color(0xFFC8E6C9)
private val CorrectText = Color(0xFF2E7D32)
private val AccentWrongBg = Color(0xFFFFE0B2)
private val AccentWrongText = Color(0xFFE65100)
private val WrongBg = Color(0xFFFFCDD2)
private val MissingUnderline = Color(0xFFEF5350)
private val ExtraText = Color(0xFF757575)
private val StarGold = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)

// Action button colors — indigo/blue for dictée (red is for poems mic)
private val DicteeAction = Color(0xFF5C6BC0)
private val DicteeActionLight = Color(0xFFC5CAE9)

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.listTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    PointsBadge(points = state.sessionScore.toLong())
                    Spacer(modifier = Modifier.width(8.dp))
                    if (state.streak > 0) {
                        StreakIndicator(streak = state.streak)
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
            if (state.isComplete) {
                SessionSummaryContent(
                    state = state,
                    onNavigateBack = onNavigateBack,
                )
            } else {
                PracticeWordContent(
                    state = state,
                    onIntent = onIntent,
                )
            }

            // Celebration overlay for correct answers
            AnimatedVisibility(
                visible = state.sessionState == DicteeSessionState.SCORED &&
                    state.feedback?.isCorrect == true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CelebrationOverlay(visible = true, onDismiss = {})
            }

            // Points fly-up animation
            AnimatedVisibility(
                visible = state.sessionState == DicteeSessionState.SCORED &&
                    state.feedback?.isCorrect == true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PointsFlyUp(
                        points = when (state.inputMode) {
                            InputMode.KEYBOARD, InputMode.LETTER_TILES -> 10
                            InputMode.HANDWRITING -> 15
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeWordContent(
    state: DicteePracticeState,
    onIntent: (DicteePracticeIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress indicator
        StudyBuddyProgressBar(
            progress = state.progress,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                CoreUiR.string.dictee_word_of,
                state.currentIndex + 1,
                state.totalWords,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Speaker button with pulse animation
        SpeakerButton(
            isPlaying = state.isPlaying,
            hasListenedOnce = state.hasListenedAtLeastOnce,
            onClick = { onIntent(DicteePracticeIntent.PlayWord) },
        )

        // Slow replay button
        if (state.hasListenedAtLeastOnce) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onIntent(DicteePracticeIntent.PlayWordSlow) },
                enabled = !state.isPlaying,
            ) {
                Text(stringResource(CoreUiR.string.dictee_slow_replay))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hint
        AnimatedVisibility(visible = state.hintVisible) {
            val word = state.currentWord
            if (word != null) {
                val maskedWord = "${word.word.first()}" + "_".repeat(word.word.length - 1)
                Text(
                    text = stringResource(CoreUiR.string.dictee_hint_format, maskedWord, word.word.length),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (!state.hintVisible && state.feedback == null && state.hasListenedAtLeastOnce) {
            OutlinedButton(onClick = { onIntent(DicteePracticeIntent.ShowHint) }) {
                Text(stringResource(CoreUiR.string.dictee_show_hint))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Processing overlay
        if (state.sessionState == DicteeSessionState.CHECKING) {
            ProcessingOverlay()
            return
        }

        // Scored state — show letter feedback + stars
        if (state.sessionState == DicteeSessionState.SCORED && state.feedback != null) {
            ScoredContent(
                score = state.feedback,
                onRetry = { onIntent(DicteePracticeIntent.RetryWord) },
                onNext = { onIntent(DicteePracticeIntent.NextWord) },
            )
            return
        }

        // Input mode selector
        if (state.hasListenedAtLeastOnce) {
            InputModeSelector(
                currentMode = state.inputMode,
                onModeSelected = { onIntent(DicteePracticeIntent.SwitchInputMode(it)) },
                enabled = state.feedback == null,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input area — dimmed if haven't listened yet
            val inputAlpha = if (state.isInputEnabled) 1f else 0.5f
            Box(modifier = Modifier.alpha(inputAlpha)) {
                when (state.inputMode) {
                    InputMode.KEYBOARD -> KeyboardInput(
                        value = state.userInput,
                        enabled = state.isInputEnabled,
                        onValueChange = { onIntent(DicteePracticeIntent.UpdateInput(it)) },
                        onDone = {
                            if (state.userInput.isNotBlank()) {
                                onIntent(DicteePracticeIntent.CheckAnswer)
                            }
                        },
                    )
                    InputMode.LETTER_TILES -> LetterTileInput(
                        answerSlots = state.answerSlots,
                        tiles = state.letterTiles,
                        enabled = state.isInputEnabled,
                        onTapTile = { onIntent(DicteePracticeIntent.TapTile(it)) },
                        onRemoveFromSlot = { onIntent(DicteePracticeIntent.RemoveFromSlot(it)) },
                    )
                    InputMode.HANDWRITING -> {
                        HandwritingCanvas(
                            modifier = Modifier.fillMaxWidth(),
                            recognizedText = state.recognizedText ?: "",
                            referenceWord = state.currentWord?.word ?: "",
                            onInkReady = { ink ->
                                onIntent(DicteePracticeIntent.RecognizeInk(ink))
                            },
                            onClear = { onIntent(DicteePracticeIntent.UpdateInput("")) },
                            onUndo = { ink ->
                                if (ink != null) {
                                    onIntent(DicteePracticeIntent.RecognizeInk(ink))
                                } else {
                                    onIntent(DicteePracticeIntent.UpdateInput(""))
                                }
                            },
                        )
                        if (state.recognitionPending) {
                            Text(
                                text = stringResource(CoreUiR.string.dictee_recognizing),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        state.recognitionErrorResId?.let { errorResId ->
                            Text(
                                text = stringResource(errorResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check + Skip buttons
            if (state.feedback == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { onIntent(DicteePracticeIntent.CheckAnswer) },
                        modifier = Modifier.weight(1f),
                        enabled = state.userInput.isNotBlank() && state.isInputEnabled,
                    ) {
                        Text(stringResource(CoreUiR.string.dictee_check))
                    }
                    OutlinedButton(
                        onClick = { onIntent(DicteePracticeIntent.SkipWord) },
                        modifier = Modifier.weight(0.5f),
                    ) {
                        Text(stringResource(CoreUiR.string.dictee_skip))
                    }
                }
            }
        }
    }
}

// ── Speaker Button with Pulse Animation ──

@Composable
private fun SpeakerButton(
    isPlaying: Boolean,
    hasListenedOnce: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val scale = if (isPlaying) {
            val infiniteTransition = rememberInfiniteTransition(label = "speakerPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "speakerScale",
            )
            pulseScale
        } else {
            1f
        }

        val fabColor = if (isPlaying) DicteeAction else MaterialTheme.colorScheme.primaryContainer

        FloatingActionButton(
            onClick = onClick,
            containerColor = fabColor,
            modifier = Modifier
                .size(72.dp)
                .scale(scale),
        ) {
            Text(
                text = "\uD83D\uDD0A",
                fontSize = 28.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPlaying) {
                stringResource(CoreUiR.string.dictee_listening)
            } else if (hasListenedOnce) {
                stringResource(CoreUiR.string.dictee_tap_to_replay)
            } else {
                stringResource(CoreUiR.string.dictee_tap_to_listen)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Input Mode Selector ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputModeSelector(
    currentMode: InputMode,
    onModeSelected: (InputMode) -> Unit,
    enabled: Boolean = true,
) {
    val modes = listOf(
        InputMode.KEYBOARD to stringResource(CoreUiR.string.dictee_keyboard),
        InputMode.LETTER_TILES to stringResource(CoreUiR.string.dictee_letter_tiles),
        InputMode.HANDWRITING to stringResource(CoreUiR.string.dictee_handwriting),
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                onClick = { onModeSelected(mode) },
                selected = currentMode == mode,
                enabled = enabled,
            ) {
                Text(label)
            }
        }
    }
}

// ── Keyboard Input ──

@Composable
private fun KeyboardInput(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(CoreUiR.string.dictee_type_word)) },
        singleLine = true,
        enabled = enabled,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(16.dp),
    )
}

// ── Processing Overlay ──

@Composable
private fun ProcessingOverlay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(CoreUiR.string.dictee_processing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Scored Content: Letter Feedback + Stars + Encouragement ──

@Composable
private fun ScoredContent(
    score: DicteeWordScore,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Star rating
        AnimatedStarRating(starCount = score.starRating)

        Spacer(modifier = Modifier.height(8.dp))

        // Encouragement message
        Text(
            text = stringResource(score.encouragementResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Letter-level feedback
        LetterFeedbackDisplay(scoredLetters = score.scoredLetters)

        Spacer(modifier = Modifier.height(12.dp))

        // Correct answer reveal
        if (!score.isCorrect) {
            Text(
                text = stringResource(CoreUiR.string.dictee_correct_spelling),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = score.referenceWord,
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons — Try Again is primary, Next is secondary
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(CoreUiR.string.dictee_try_again))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(CoreUiR.string.dictee_next_word))
        }
    }
}

// ── Animated Star Rating (identical to poems) ──

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
                tint = StarGold,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(alpha),
            )
        }
    }
}

// ── Letter Feedback Display ──

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterFeedbackDisplay(
    scoredLetters: List<ScoredLetter>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        scoredLetters.forEachIndexed { index, scored ->
            val delayMs = index * 100
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, delayMillis = delayMs),
                label = "letter_$index",
            )

            Box(modifier = Modifier.alpha(alpha)) {
                ScoredLetterCard(scored)
            }
        }
    }
}

@Composable
private fun ScoredLetterCard(scored: ScoredLetter) {
    val backgroundColor = when (scored.status) {
        LetterStatus.CORRECT -> CorrectBg
        LetterStatus.ACCENT_WRONG -> AccentWrongBg
        LetterStatus.WRONG -> WrongBg
        LetterStatus.MISSING -> Color.Transparent
        LetterStatus.EXTRA -> Color.Transparent
    }

    val textColor = when (scored.status) {
        LetterStatus.CORRECT -> CorrectText
        LetterStatus.ACCENT_WRONG -> AccentWrongText
        LetterStatus.WRONG -> MaterialTheme.colorScheme.error
        LetterStatus.MISSING -> MissingUnderline
        LetterStatus.EXTRA -> ExtraText
    }

    val textDecoration = when (scored.status) {
        LetterStatus.MISSING -> TextDecoration.Underline
        else -> TextDecoration.None
    }

    val displayChar = when (scored.status) {
        LetterStatus.MISSING -> scored.referenceCharacter?.toString() ?: "_"
        LetterStatus.EXTRA -> scored.character?.toString() ?: ""
        else -> scored.character?.toString() ?: "_"
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (scored.status == LetterStatus.MISSING) {
                    Modifier.background(
                        MissingUnderline.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp),
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = displayChar,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = textColor,
            textDecoration = textDecoration,
        )
    }
}

// ── Session Summary Content ──

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionSummaryContent(
    state: DicteePracticeState,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(CoreUiR.string.dictee_session_complete),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(CoreUiR.string.dictee_session_finished, state.listTitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Average star rating
        if (state.averageStars > 0) {
            AnimatedStarRating(starCount = state.averageStars)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Points
        Text(
            text = stringResource(CoreUiR.string.dictee_score_points, state.sessionScore),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Word-by-word results
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.sessionResults.forEach { result ->
                WordResultChip(result)
            }
        }

        // Words to practice
        val missedWords = state.sessionResults.filter { !it.isCorrect }
        if (missedWords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(CoreUiR.string.dictee_words_to_practice),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = missedWords.joinToString(", ") { it.referenceWord },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(CoreUiR.string.dictee_handwriting_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Done button
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(CoreUiR.string.dictee_done))
        }
    }
}

@Composable
private fun WordResultChip(result: DicteeWordScore) {
    val bgColor = if (result.isCorrect) CorrectBg else AccentWrongBg
    val icon = if (result.isCorrect) "\u2705" else "\u26A0\uFE0F"
    val modeIcon = when (result.inputMode) {
        InputMode.KEYBOARD -> "\u2328\uFE0F"
        InputMode.HANDWRITING -> "\u270D\uFE0F"
        InputMode.LETTER_TILES -> "\uD83E\uDDE9"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$icon ${result.referenceWord} $modeIcon",
            style = MaterialTheme.typography.bodyMedium,
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
            hasListenedAtLeastOnce = true,
            sessionState = DicteeSessionState.TYPING,
        ),
        onIntent = {},
        onNavigateBack = {},
    )
}
