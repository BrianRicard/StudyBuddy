package com.studybuddy.feature.math.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StreakOrange
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import kotlinx.coroutines.delay

@Composable
fun MathResultsScreen(
    viewModel: MathResultsViewModel = hiltViewModel(),
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effect.collectAsState()

    LaunchedEffect(effect) {
        when (effect) {
            MathResultsEffect.NavigateToHome -> {
                viewModel.consumeEffect()
                onHome()
            }
            MathResultsEffect.NavigateToSetup -> {
                viewModel.consumeEffect()
                onPlayAgain()
            }
            null -> Unit
        }
    }

    MathResultsContent(
        state = state,
        onHome = { viewModel.onIntent(MathResultsIntent.NavigateHome) },
        onPlayAgain = { viewModel.onIntent(MathResultsIntent.PlayAgain) },
    )
}

@Composable
internal fun MathResultsContent(
    state: MathResultsState,
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(ANIMATION_DELAY_MS)
        visible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(CoreUiR.string.math_great_job),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 2 },
        ) {
            SummaryGrid(state = state)
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 2 },
        ) {
            PointsBreakdownCard(state = state)
        }

        if (state.badges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                BadgesCard(badges = state.badges)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))

        ActionButtons(
            onHome = onHome,
            onPlayAgain = onPlayAgain,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryGrid(
    state: MathResultsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryStatCard(
                label = stringResource(CoreUiR.string.math_score_label),
                value = "${state.correctCount}/${state.totalProblems}",
                icon = "\uD83C\uDFAF",
                modifier = Modifier.weight(1f),
            )
            SummaryStatCard(
                label = stringResource(CoreUiR.string.math_accuracy_label),
                value = "${(state.accuracy * PERCENTAGE_MULTIPLIER).toInt()}%",
                icon = "\u2705",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryStatCard(
                label = stringResource(CoreUiR.string.math_best_streak_label),
                value = state.bestStreak.toString(),
                icon = "\uD83D\uDD25",
                modifier = Modifier.weight(1f),
            )
            SummaryStatCard(
                label = stringResource(CoreUiR.string.math_avg_time_label),
                value = formatResponseTime(state.avgResponseMs),
                icon = "\u23F1\uFE0F",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = LABEL_ALPHA,
                ),
            )
        }
    }
}

@Composable
private fun PointsBreakdownCard(
    state: MathResultsState,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(CoreUiR.string.math_points_earned),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PointsRow(
                label = stringResource(CoreUiR.string.math_base_points),
                value = "${state.sessionScore} pts",
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PointsRow(
                label = stringResource(CoreUiR.string.math_streak_bonus),
                value = "+${state.streakBonus} pts",
                color = StreakOrange,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = DIVIDER_ALPHA,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(CoreUiR.string.math_total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${state.totalPoints} pts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PointsGold,
                )
            }
        }
    }
}

@Composable
private fun PointsRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = LABEL_ALPHA,
            ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun BadgesCard(
    badges: List<String>,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(CoreUiR.string.math_badges_unlocked),
                style = MaterialTheme.typography.titleLarge,
                color = CorrectGreen,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            badges.forEach { badge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = badgeIcon(badge),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        StudyBuddyOutlinedButton(
            text = stringResource(CoreUiR.string.math_go_home),
            onClick = onHome,
            modifier = Modifier.weight(1f),
        )
        StudyBuddyButton(
            text = stringResource(CoreUiR.string.math_play_again),
            onClick = onPlayAgain,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun badgeIcon(badge: String): String = when (badge) {
    MathResultsViewModel.BADGE_SPEED_DEMON -> "\u26A1"
    MathResultsViewModel.BADGE_STREAK_MASTER -> "\uD83D\uDD25"
    else -> "\uD83C\uDFC6"
}

private fun formatResponseTime(ms: Long): String {
    val seconds = ms / MS_PER_SECOND.toDouble()
    return "%.1fs".format(seconds)
}

private const val ANIMATION_DELAY_MS = 150L
private const val PERCENTAGE_MULTIPLIER = 100
private const val MS_PER_SECOND = 1000
private const val LABEL_ALPHA = 0.7f
private const val DIVIDER_ALPHA = 0.12f

@Preview(showBackground = true)
@Composable
private fun MathResultsContentPreview() {
    StudyBuddyTheme {
        MathResultsContent(
            state = MathResultsState(
                totalProblems = 20,
                correctCount = 18,
                bestStreak = 12,
                avgResponseMs = 4200,
                sessionScore = 90,
                streakBonus = 75,
                totalPoints = 165,
                accuracy = 0.9f,
                badges = listOf("Streak Master"),
            ),
            onHome = {},
            onPlayAgain = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathResultsContentNoBadgesPreview() {
    StudyBuddyTheme {
        MathResultsContent(
            state = MathResultsState(
                totalProblems = 10,
                correctCount = 6,
                bestStreak = 3,
                avgResponseMs = 5500,
                sessionScore = 30,
                streakBonus = 0,
                totalPoints = 30,
                accuracy = 0.6f,
                badges = emptyList(),
            ),
            onHome = {},
            onPlayAgain = {},
        )
    }
}
