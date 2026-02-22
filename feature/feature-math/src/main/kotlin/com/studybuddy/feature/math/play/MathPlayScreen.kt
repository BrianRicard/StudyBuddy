package com.studybuddy.feature.math.play

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.ui.components.StreakIndicator
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.IncorrectRed
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun MathPlayScreen(
    viewModel: MathPlayViewModel = hiltViewModel(),
    onGameComplete: (
        totalProblems: Int,
        correctCount: Int,
        bestStreak: Int,
        avgResponseMs: Long,
        sessionScore: Int,
        operators: String,
        rangeMin: Int,
        rangeMax: Int,
    ) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            val avgMs = if (state.responseTimesMs.isNotEmpty()) {
                state.responseTimesMs.average().toLong()
            } else {
                0L
            }
            onGameComplete(
                state.totalProblems,
                state.correctCount,
                state.bestStreak,
                avgMs,
                state.sessionScore,
                state.operatorNames,
                state.rangeMin,
                state.rangeMax,
            )
        }
    }

    MathPlayContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun MathPlayContent(
    state: MathPlayState,
    onIntent: (MathPlayIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar: progress + pause
            TopBar(
                problemsCompleted = state.problemsCompleted,
                totalProblems = state.totalProblems,
                onPause = { onIntent(MathPlayIntent.Pause) },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timer bar
            TimerBar(
                timeRemainingMs = state.timeRemainingMs,
                timerTotal = state.timerTotal,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Streak indicator
            if (state.streak > 0) {
                StreakIndicator(streak = state.streak)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Problem display
            ProblemDisplay(
                problem = state.currentProblem,
                feedback = state.feedback,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Answer display
            AnswerDisplay(
                userAnswer = state.userAnswer,
                feedback = state.feedback,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Feedback message
            FeedbackMessage(feedback = state.feedback)

            Spacer(modifier = Modifier.height(16.dp))

            // Number pad
            NumberPad(
                onDigit = { onIntent(MathPlayIntent.DigitEntered(it)) },
                onBackspace = { onIntent(MathPlayIntent.Backspace) },
                onSubmit = { onIntent(MathPlayIntent.Submit) },
                enabled = state.feedback == null && !state.isPaused,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pause dialog overlay
        if (state.isPaused) {
            PauseDialog(
                onResume = { onIntent(MathPlayIntent.Resume) },
            )
        }

        // Celebration overlay
        AnimatedVisibility(
            visible = state.showCelebration,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            CelebrationOverlay(streak = state.streak)
        }
    }
}

@Composable
private fun TopBar(
    problemsCompleted: Int,
    totalProblems: Int,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$problemsCompleted / $totalProblems",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val progress = if (totalProblems > 0) {
            problemsCompleted.toFloat() / totalProblems.toFloat()
        } else {
            0f
        }
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = PROGRESS_ANIM_MS),
            label = "progress",
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        IconButton(
            onClick = onPause,
            modifier = Modifier.semantics {
                contentDescription = "Pause game"
            },
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun TimerBar(
    timeRemainingMs: Long,
    timerTotal: Long,
    modifier: Modifier = Modifier,
) {
    if (timerTotal <= 0) return

    val fraction = (timeRemainingMs.toFloat() / timerTotal.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = TIMER_ANIM_MS),
        label = "timer",
    )

    val timerColor = when {
        fraction <= TIMER_CRITICAL_THRESHOLD -> IncorrectRed
        fraction <= TIMER_WARNING_THRESHOLD -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val seconds = (timeRemainingMs / MS_PER_SECOND).toInt()
    val tenths = ((timeRemainingMs % MS_PER_SECOND) / TENTHS_DIVISOR).toInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = timerColor,
            trackColor = timerColor.copy(alpha = TIMER_TRACK_ALPHA),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$seconds.${tenths}s",
            style = MaterialTheme.typography.bodySmall,
            color = timerColor,
        )
    }
}

@Composable
private fun ProblemDisplay(
    problem: MathProblem?,
    feedback: Feedback?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = problem,
        transitionSpec = {
            (slideInVertically { -it } + fadeIn()) togetherWith
                (slideOutVertically { it } + fadeOut())
        },
        label = "problem",
        modifier = modifier,
    ) { currentProblem ->
        if (currentProblem != null) {
            Text(
                text = currentProblem.displayString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = PROBLEM_FONT_SIZE.sp,
                ),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription =
                        "${currentProblem.operandA} ${currentProblem.operator.symbol} ${currentProblem.operandB}"
                },
            )
        }
    }
}

@Composable
private fun AnswerDisplay(
    userAnswer: String,
    feedback: Feedback?,
    modifier: Modifier = Modifier,
) {
    val answerColor = when (feedback) {
        is Feedback.Correct -> CorrectGreen
        is Feedback.Incorrect, is Feedback.TimeUp -> IncorrectRed
        null -> MaterialTheme.colorScheme.onSurface
    }

    val displayText = when {
        feedback is Feedback.Incorrect -> feedback.correctAnswer
        feedback is Feedback.TimeUp -> "?"
        userAnswer.isEmpty() -> "_"
        else -> userAnswer
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ANSWER_BOX_HEIGHT.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ANSWER_BG_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = ANSWER_FONT_SIZE.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = answerColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeedbackMessage(
    feedback: Feedback?,
    modifier: Modifier = Modifier,
) {
    val message = when (feedback) {
        is Feedback.Correct -> ENCOURAGEMENTS.random()
        is Feedback.Incorrect -> "The answer was ${feedback.correctAnswer}"
        is Feedback.TimeUp -> "Time's up! Keep going!"
        null -> ""
    }

    val color = when (feedback) {
        is Feedback.Correct -> CorrectGreen
        is Feedback.Incorrect, is Feedback.TimeUp -> IncorrectRed
        null -> MaterialTheme.colorScheme.onSurface
    }

    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NumberPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Row 1: 1 2 3
        NumberPadRow(digits = listOf(1, 2, 3), onDigit = onDigit, enabled = enabled)
        // Row 2: 4 5 6
        NumberPadRow(digits = listOf(4, 5, 6), onDigit = onDigit, enabled = enabled)
        // Row 3: 7 8 9
        NumberPadRow(digits = listOf(7, 8, 9), onDigit = onDigit, enabled = enabled)
        // Row 4: backspace 0 submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            // Backspace
            Box(
                modifier = Modifier
                    .size(NUMPAD_BUTTON_SIZE.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = enabled) { onBackspace() }
                    .semantics { contentDescription = "Backspace" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = null,
                    modifier = Modifier.size(NUMPAD_ICON_SIZE.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 0
            NumPadButton(digit = 0, onClick = { onDigit(0) }, enabled = enabled)

            // Submit
            Box(
                modifier = Modifier
                    .size(NUMPAD_BUTTON_SIZE.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = enabled) { onSubmit() }
                    .semantics { contentDescription = "Submit answer" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(NUMPAD_ICON_SIZE.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun NumberPadRow(
    digits: List<Int>,
    onDigit: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        digits.forEach { digit ->
            NumPadButton(digit = digit, onClick = { onDigit(digit) }, enabled = enabled)
        }
    }
}

@Composable
private fun NumPadButton(
    digit: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(NUMPAD_BUTTON_SIZE.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "Digit $digit" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
            },
        )
    }
}

@Composable
private fun PauseDialog(onResume: () -> Unit) {
    AlertDialog(
        onDismissRequest = onResume,
        title = {
            Text(
                text = "Game Paused",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = "Take a break! Tap Resume when you're ready to continue.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text("Resume")
            }
        },
    )
}

@Composable
private fun CelebrationOverlay(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when {
                streak >= STREAK_MILESTONE_20 -> "\uD83C\uDF1F"
                streak >= STREAK_MILESTONE_10 -> "\uD83D\uDD25"
                else -> "\u2B50"
            },
            fontSize = CELEBRATION_ICON_SIZE.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$streak streak!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private val ENCOURAGEMENTS = listOf(
    "Great job!",
    "Awesome!",
    "You got it!",
    "Well done!",
    "Fantastic!",
    "Super!",
    "Perfect!",
    "Amazing!",
)

private const val PROGRESS_ANIM_MS = 300
private const val TIMER_ANIM_MS = 100
private const val TIMER_CRITICAL_THRESHOLD = 0.2f
private const val TIMER_WARNING_THRESHOLD = 0.5f
private const val TIMER_TRACK_ALPHA = 0.2f
private const val MS_PER_SECOND = 1000
private const val TENTHS_DIVISOR = 100
private const val PROBLEM_FONT_SIZE = 48
private const val ANSWER_BOX_HEIGHT = 80
private const val ANSWER_FONT_SIZE = 40
private const val ANSWER_BG_ALPHA = 0.5f
private const val NUMPAD_BUTTON_SIZE = 72
private const val NUMPAD_ICON_SIZE = 28
private const val DISABLED_ALPHA = 0.4f
private const val CELEBRATION_ICON_SIZE = 64
private const val STREAK_MILESTONE_5 = 5
private const val STREAK_MILESTONE_10 = 10
private const val STREAK_MILESTONE_20 = 20

@Preview(showBackground = true)
@Composable
private fun MathPlayContentPreview() {
    StudyBuddyTheme {
        MathPlayContent(
            state = MathPlayState(
                currentProblem = MathProblem(
                    operandA = 7,
                    operandB = 3,
                    operator = Operator.PLUS,
                    correctAnswer = 10,
                ),
                userAnswer = "10",
                problemsCompleted = 5,
                totalProblems = 20,
                streak = 3,
                timeRemainingMs = 8_000,
                timerTotal = 15_000,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPlayContentCorrectPreview() {
    StudyBuddyTheme {
        MathPlayContent(
            state = MathPlayState(
                currentProblem = MathProblem(
                    operandA = 12,
                    operandB = 4,
                    operator = Operator.MULTIPLY,
                    correctAnswer = 48,
                ),
                userAnswer = "48",
                feedback = Feedback.Correct,
                problemsCompleted = 8,
                totalProblems = 20,
                streak = 5,
                correctCount = 8,
                timeRemainingMs = 10_000,
                timerTotal = 15_000,
            ),
            onIntent = {},
        )
    }
}
