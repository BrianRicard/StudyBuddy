package com.studybuddy.feature.conjugation.boss

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.CelebrationOverlay
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.modifier.shake
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.conjugation.components.QuestCreature
import com.studybuddy.feature.conjugation.components.StepProgressBar

@Composable
fun BossScreen(
    onNavigateBack: () -> Unit,
    viewModel: BossViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BossEffect.Completed -> onNavigateBack()
            }
        }
    }

    BossContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BossContent(
    state: BossState,
    onIntent: (BossIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.conjugation_boss_title)) },
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
            QuestCreature(characterId = stage.bossCharacterId, size = 120.dp)
            Spacer(Modifier.height(12.dp))

            when (state.phase) {
                BossPhase.INTRO -> IntroSection(state = state, onIntent = onIntent)
                BossPhase.BUILD, BossPhase.SENTENCE_DONE -> BuildSection(state = state, onIntent = onIntent)
                BossPhase.WON -> WonSection(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun IntroSection(
    state: BossState,
    onIntent: (BossIntent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (state.isFinalStage) {
                    CoreUiR.string.conjugation_boss_intro_final
                } else {
                    CoreUiR.string.conjugation_boss_intro
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        StudyBuddyButton(
            text = stringResource(CoreUiR.string.conjugation_boss_listen),
            onClick = { onIntent(BossIntent.StartBuilding) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BuildSection(
    state: BossState,
    onIntent: (BossIntent) -> Unit,
) {
    val sentenceDone = state.phase == BossPhase.SENTENCE_DONE

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StepProgressBar(
            current = state.sentenceIndex + if (sentenceDone) 1 else 0,
            total = state.sentences.size,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                CoreUiR.string.conjugation_boss_progress,
                state.sentenceIndex + 1,
                state.sentences.size,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        IconButton(onClick = { onIntent(BossIntent.PlayRap) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = stringResource(CoreUiR.string.conjugation_boss_listen),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        // The sentence being rebuilt, one slot per word.
        Text(
            text = state.targetWords.indices.joinToString(" ") { index ->
                state.builtWords.getOrNull(index) ?: "____"
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        if (sentenceDone) {
            Text(
                text = stringResource(CoreUiR.string.conjugation_boss_sentence_done),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            StudyBuddyButton(
                text = stringResource(CoreUiR.string.conjugation_next),
                onClick = { onIntent(BossIntent.NextSentence) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = stringResource(CoreUiR.string.conjugation_boss_rebuild),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                state.bank.filterNot { it.isUsed }.forEach { chip ->
                    AssistChip(
                        onClick = { onIntent(BossIntent.TapChip(chip.id)) },
                        label = {
                            Text(chip.text, style = MaterialTheme.typography.titleMedium)
                        },
                        modifier = Modifier.shake(trigger = state.shakeChipId == chip.id),
                    )
                }
            }
            if (state.shakeChipId != null) {
                Text(
                    text = stringResource(CoreUiR.string.conjugation_boss_encourage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WonSection(
    state: BossState,
    onIntent: (BossIntent) -> Unit,
) {
    CelebrationOverlay(visible = true)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (state.isFinalStage) {
                    CoreUiR.string.conjugation_boss_won_final
                } else {
                    CoreUiR.string.conjugation_boss_won
                },
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
        )
        StudyBuddyButton(
            text = stringResource(CoreUiR.string.conjugation_next),
            onClick = { onIntent(BossIntent.Finish) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BossPreview() {
    StudyBuddyTheme {
        BossContent(
            state = BossState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
