package com.studybuddy.feature.math.challenge

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.theme.IncorrectRed
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import kotlin.math.roundToInt

@Composable
fun MathChallengeScreen(
    viewModel: MathChallengeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MathChallengeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onNavigateHome = onNavigateHome,
    )
}

@Composable
internal fun MathChallengeContent(
    state: MathChallengeState,
    onIntent: (MathChallengeIntent) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            ChallengeTopBar(
                lives = state.lives,
                bombs = state.bombs,
                score = state.score,
                level = state.level,
                streak = state.streak,
                multiplier = state.multiplier,
                onPause = { onIntent(MathChallengeIntent.Pause) },
                onBomb = { onIntent(MathChallengeIntent.UseBomb) },
            )

            // Game area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                GameArea(
                    equations = state.equations,
                    modifier = Modifier.fillMaxSize(),
                )

                // Bomb flash overlay
                if (state.showBombFlash) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = BOMB_FLASH_ALPHA)),
                    )
                }
            }

            // Answer display
            AnswerField(userAnswer = state.userAnswer)

            // Number pad
            ChallengeNumberPad(
                onDigit = { onIntent(MathChallengeIntent.DigitEntered(it)) },
                onBackspace = { onIntent(MathChallengeIntent.Backspace) },
                onSubmit = { onIntent(MathChallengeIntent.Submit) },
                enabled = !state.isGameOver && !state.isPaused,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Game over overlay
        if (state.isGameOver) {
            GameOverOverlay(
                score = state.score,
                equationsSolved = state.equationsSolved,
                level = state.level,
                bestStreak = state.bestStreak,
                onPlayAgain = { onIntent(MathChallengeIntent.PlayAgain) },
                onHome = onNavigateHome,
            )
        }

        // Pause dialog
        if (state.isPaused) {
            PauseChallengeDialog(
                onResume = { onIntent(MathChallengeIntent.Resume) },
                onQuit = onNavigateBack,
            )
        }
    }
}

@Composable
private fun ChallengeTopBar(
    lives: Int,
    bombs: Int,
    score: Int,
    level: Int,
    streak: Int,
    multiplier: Float,
    onPause: () -> Unit,
    onBomb: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lives (hearts)
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(lives) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = IncorrectRed,
                    modifier = Modifier.size(20.dp),
                )
            }
            repeat((MathChallengeState.INITIAL_LIVES - lives).coerceAtLeast(0)) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = IncorrectRed.copy(alpha = EMPTY_HEART_ALPHA),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Score + Level
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(CoreUiR.string.challenge_score_value, score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(CoreUiR.string.challenge_level, level),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (streak >= MathChallengeState.STREAK_1_5X) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(CoreUiR.string.challenge_multiplier, multiplier),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Bomb button
        val bombDesc = stringResource(CoreUiR.string.challenge_use_bomb)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (bombs > 0) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                )
                .clickable(enabled = bombs > 0) { onBomb() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .semantics { contentDescription = bombDesc },
        ) {
            Text(
                text = stringResource(CoreUiR.string.challenge_bombs, bombs),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (bombs > 0) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
                },
            )
        }

        // Pause
        IconButton(onClick = onPause) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = stringResource(CoreUiR.string.math_pause_game),
            )
        }
    }
}

@Composable
private fun GameArea(
    equations: List<FallingEquation>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var areaWidthPx = 0f
    var areaHeightPx = 0f

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                areaWidthPx = size.width.toFloat()
                areaHeightPx = size.height.toFloat()
            }
            .drawBehind {
                // Danger zone indicator at bottom
                drawRect(
                    color = Color.Red.copy(alpha = DANGER_ZONE_ALPHA),
                    topLeft = Offset(0f, size.height * DANGER_ZONE_START),
                    size = androidx.compose.ui.geometry.Size(
                        size.width,
                        size.height * (1f - DANGER_ZONE_START),
                    ),
                )
            },
    ) {
        equations.forEach { equation ->
            val cardWidth = if (equation.isRainbow) RAINBOW_CARD_WIDTH else CARD_WIDTH
            val maxXOffset = with(density) { (areaWidthPx - cardWidth.toPx()).coerceAtLeast(0f) }

            val xOffset = if (equation.isRainbow) {
                (equation.xProgress * maxXOffset).roundToInt()
            } else {
                // Use equation id to deterministically place cards horizontally
                val fraction = (equation.id % HORIZONTAL_SLOTS).toFloat() / HORIZONTAL_SLOTS
                (fraction * maxXOffset).roundToInt()
            }

            val yOffset = with(density) {
                (equation.yProgress * (areaHeightPx - CARD_HEIGHT.toPx())).roundToInt()
            }

            EquationCard(
                equation = equation,
                modifier = Modifier.offset { IntOffset(xOffset, yOffset) },
            )
        }

        // Empty state hint
        if (equations.isEmpty()) {
            Text(
                text = stringResource(CoreUiR.string.challenge_get_ready),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HINT_ALPHA),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun EquationCard(
    equation: FallingEquation,
    modifier: Modifier = Modifier,
) {
    if (equation.isRainbow) {
        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        val hueShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = RAINBOW_ANIM_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "hue",
        )

        val rainbowBrush = Brush.linearGradient(
            colors = listOf(
                Color.Red.copy(alpha = RAINBOW_COLOR_ALPHA),
                Color.Yellow.copy(alpha = RAINBOW_COLOR_ALPHA),
                Color.Green.copy(alpha = RAINBOW_COLOR_ALPHA),
                Color.Cyan.copy(alpha = RAINBOW_COLOR_ALPHA),
                Color.Blue.copy(alpha = RAINBOW_COLOR_ALPHA),
                Color.Magenta.copy(alpha = RAINBOW_COLOR_ALPHA),
            ),
            start = Offset(hueShift * RAINBOW_GRADIENT_SCALE, 0f),
            end = Offset(hueShift * RAINBOW_GRADIENT_SCALE + RAINBOW_GRADIENT_SCALE, 0f),
        )

        Box(
            modifier = modifier
                .size(width = RAINBOW_CARD_WIDTH, height = CARD_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
                .background(rainbowBrush),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${equation.problem.displayString} = ?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    } else {
        ElevatedCard(
            modifier = modifier.size(width = CARD_WIDTH, height = CARD_HEIGHT),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${equation.problem.displayString} = ?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AnswerField(
    userAnswer: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(ANSWER_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ANSWER_BG_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = userAnswer.ifEmpty { "_" },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (userAnswer.isNotEmpty()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChallengeNumberPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NumberRow(digits = listOf(1, 2, 3), onDigit = onDigit, enabled = enabled)
        NumberRow(digits = listOf(4, 5, 6), onDigit = onDigit, enabled = enabled)
        NumberRow(digits = listOf(7, 8, 9), onDigit = onDigit, enabled = enabled)

        // Bottom row: backspace, 0, submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            val backspaceDesc = stringResource(CoreUiR.string.math_backspace)
            Box(
                modifier = Modifier
                    .size(NUMPAD_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = enabled) { onBackspace() }
                    .semantics { contentDescription = backspaceDesc },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = null,
                    modifier = Modifier.size(NUMPAD_ICON_SIZE),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PadButton(digit = 0, onClick = { onDigit(0) }, enabled = enabled)

            val submitDesc = stringResource(CoreUiR.string.math_submit_answer)
            Box(
                modifier = Modifier
                    .size(NUMPAD_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = enabled) { onSubmit() }
                    .semantics { contentDescription = submitDesc },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(NUMPAD_ICON_SIZE),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun NumberRow(
    digits: List<Int>,
    onDigit: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        digits.forEach { digit ->
            PadButton(digit = digit, onClick = { onDigit(digit) }, enabled = enabled)
        }
    }
}

@Composable
private fun PadButton(
    digit: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val digitDesc = stringResource(CoreUiR.string.math_digit, digit)
    Box(
        modifier = modifier
            .size(NUMPAD_SIZE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = digitDesc },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.headlineSmall,
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
private fun GameOverOverlay(
    score: Int,
    equationsSolved: Int,
    level: Int,
    bestStreak: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = OVERLAY_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        StudyBuddyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(CoreUiR.string.challenge_game_over),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem(
                        label = stringResource(CoreUiR.string.math_score_label),
                        value = score.toString(),
                    )
                    StatItem(
                        label = stringResource(CoreUiR.string.challenge_solved),
                        value = equationsSolved.toString(),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem(
                        label = stringResource(CoreUiR.string.challenge_level_reached),
                        value = level.toString(),
                    )
                    StatItem(
                        label = stringResource(CoreUiR.string.math_best_streak_label),
                        value = bestStreak.toString(),
                    )
                }

                // Encouragement
                Text(
                    text = gameOverMessage(equationsSolved),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                StudyBuddyButton(
                    text = stringResource(CoreUiR.string.math_play_again),
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth(),
                )

                TextButton(onClick = onHome) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(CoreUiR.string.math_go_home))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun gameOverMessage(solved: Int): String = when {
    solved >= TIER_AMAZING -> stringResource(CoreUiR.string.challenge_msg_amazing)
    solved >= TIER_GREAT -> stringResource(CoreUiR.string.challenge_msg_great)
    solved >= TIER_GOOD -> stringResource(CoreUiR.string.challenge_msg_good)
    else -> stringResource(CoreUiR.string.challenge_msg_try_again)
}

@Composable
private fun PauseChallengeDialog(
    onResume: () -> Unit,
    onQuit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onResume,
        title = {
            Text(
                text = stringResource(CoreUiR.string.math_game_paused),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = stringResource(CoreUiR.string.math_pause_message),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text(stringResource(CoreUiR.string.math_resume))
            }
        },
        dismissButton = {
            TextButton(onClick = onQuit) {
                Text(stringResource(CoreUiR.string.math_quit))
            }
        },
    )
}

// Constants
private val CARD_WIDTH = 140.dp
private val CARD_HEIGHT = 56.dp
private val RAINBOW_CARD_WIDTH = 160.dp
private val ANSWER_HEIGHT = 56.dp
private val NUMPAD_SIZE = 64.dp
private val NUMPAD_ICON_SIZE = 24.dp
private const val HORIZONTAL_SLOTS = 5
private const val BOMB_FLASH_ALPHA = 0.6f
private const val EMPTY_HEART_ALPHA = 0.3f
private const val DISABLED_ALPHA = 0.4f
private const val DANGER_ZONE_ALPHA = 0.05f
private const val DANGER_ZONE_START = 0.85f
private const val ANSWER_BG_ALPHA = 0.5f
private const val OVERLAY_ALPHA = 0.7f
private const val HINT_ALPHA = 0.5f
private const val RAINBOW_ANIM_MS = 2000
private const val RAINBOW_COLOR_ALPHA = 0.8f
private const val RAINBOW_GRADIENT_SCALE = 300f
private const val TIER_AMAZING = 30
private const val TIER_GREAT = 15
private const val TIER_GOOD = 5

@Preview(showBackground = true)
@Composable
private fun MathChallengePreview() {
    StudyBuddyTheme {
        MathChallengeContent(
            state = MathChallengeState(
                equations = listOf(
                    FallingEquation(
                        id = 0,
                        problem = MathProblem(3, 5, Operator.PLUS, 8),
                        yProgress = 0.2f,
                    ),
                    FallingEquation(
                        id = 1,
                        problem = MathProblem(7, 2, Operator.MINUS, 5),
                        yProgress = 0.5f,
                    ),
                    FallingEquation(
                        id = 2,
                        problem = MathProblem(4, 6, Operator.MULTIPLY, 24),
                        yProgress = 0.1f,
                        isRainbow = true,
                    ),
                ),
                userAnswer = "8",
                lives = 3,
                bombs = 2,
                score = 150,
                streak = 5,
                level = 2,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameOverPreview() {
    StudyBuddyTheme {
        MathChallengeContent(
            state = MathChallengeState(
                isGameOver = true,
                score = 450,
                equationsSolved = 23,
                level = 3,
                bestStreak = 8,
            ),
            onIntent = {},
        )
    }
}
