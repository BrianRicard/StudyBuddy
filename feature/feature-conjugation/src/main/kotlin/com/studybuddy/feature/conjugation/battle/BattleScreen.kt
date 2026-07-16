package com.studybuddy.feature.conjugation.battle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.CelebrationOverlay
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.conjugation.components.QuestCreature
import com.studybuddy.feature.conjugation.components.praiseRes

@Composable
fun BattleScreen(
    onNavigateBack: () -> Unit,
    viewModel: BattleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BattleEffect.Completed -> onNavigateBack()
            }
        }
    }

    BattleContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BattleContent(
    state: BattleState,
    onIntent: (BattleIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.conjugation_battle_title)) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FriendWithCheerMeter(state = state, friendCharacterId = stage.friendCharacterId)
            Spacer(Modifier.height(16.dp))

            when (state.phase) {
                BattlePhase.QUESTION -> QuestionSection(state = state, onIntent = onIntent)
                BattlePhase.CORRECT -> FeedbackSection(
                    message = stringResource(CoreUiR.string.conjugation_battle_correct),
                    praise = stringResource(praiseRes(state.cheeredCount)),
                    buttonText = stringResource(CoreUiR.string.conjugation_next),
                    onContinue = { onIntent(BattleIntent.Continue) },
                )

                BattlePhase.ENCOURAGE -> FeedbackSection(
                    message = stringResource(CoreUiR.string.conjugation_battle_encourage),
                    praise = null,
                    buttonText = stringResource(CoreUiR.string.conjugation_next),
                    onContinue = { onIntent(BattleIntent.Continue) },
                )

                BattlePhase.GIFT -> GiftSection()
                BattlePhase.WON -> WonSection(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun FriendWithCheerMeter(
    state: BattleState,
    friendCharacterId: String,
) {
    val cheer by animateFloatAsState(
        targetValue = state.cheerProgress,
        animationSpec = tween(durationMillis = 400),
        label = "cheer",
    )
    val friendScale by animateFloatAsState(
        targetValue = if (state.phase == BattlePhase.QUESTION) 1f else 1.1f,
        label = "friendScale",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        QuestCreature(
            characterId = friendCharacterId,
            modifier = Modifier.scale(friendScale),
            size = 110.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(CoreUiR.string.conjugation_battle_cheer_meter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { cheer },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp),
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun QuestionSection(
    state: BattleState,
    onIntent: (BattleIntent) -> Unit,
) {
    val round = state.currentRound ?: return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (round.isReview) {
            Text(
                text = stringResource(CoreUiR.string.conjugation_battle_review_tag),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = stringResource(CoreUiR.string.conjugation_battle_prompt),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${round.promptPronoun} ___  (${round.verb.infinitive})",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        round.options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { option ->
                    OutlinedButton(
                        onClick = { onIntent(BattleIntent.SelectOption(option)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    ) {
                        Text(option, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FeedbackSection(
    message: String,
    praise: String?,
    buttonText: String,
    onContinue: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (praise != null) {
            Text(
                text = praise,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        StudyBuddyButton(
            text = buttonText,
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GiftSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // The ladybug gift: the new ladybug creature handed to the friend.
        QuestCreature(characterId = "ladybug", size = 72.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(CoreUiR.string.conjugation_battle_gift),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WonSection(
    state: BattleState,
    onIntent: (BattleIntent) -> Unit,
) {
    CelebrationOverlay(visible = true)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(CoreUiR.string.conjugation_battle_won),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(CoreUiR.string.conjugation_battle_gift),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        StudyBuddyButton(
            text = stringResource(CoreUiR.string.conjugation_next),
            onClick = { onIntent(BattleIntent.Finish) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BattlePreview() {
    StudyBuddyTheme {
        BattleContent(
            state = BattleState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
