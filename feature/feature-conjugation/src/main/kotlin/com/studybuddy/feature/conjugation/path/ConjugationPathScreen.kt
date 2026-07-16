package com.studybuddy.feature.conjugation.path

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.modifier.accessibleClickable
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.conjugation.components.QuestCreature
import com.studybuddy.feature.conjugation.components.stageStoryRes
import com.studybuddy.feature.conjugation.components.stageTitleRes
import com.studybuddy.feature.conjugation.components.stepLabelRes

@Composable
fun ConjugationPathScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStep: (String, ConjugationStep) -> Unit,
    viewModel: ConjugationPathViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ConjugationPathEffect.NavigateToStep ->
                    onNavigateToStep(effect.stageId, effect.step)
            }
        }
    }

    ConjugationPathContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConjugationPathContent(
    state: ConjugationPathState,
    onIntent: (ConjugationPathIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.conjugation_title)) },
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(
                        if (state.isQuestComplete) {
                            CoreUiR.string.conjugation_quest_complete
                        } else {
                            CoreUiR.string.conjugation_subtitle
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            items(state.stages, key = { it.stage.id }) { pathStage ->
                StageNode(
                    pathStage = pathStage,
                    isExpanded = state.expandedStageId == pathStage.stage.id,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun StageNode(
    pathStage: ConjugationPathStage,
    isExpanded: Boolean,
    onIntent: (ConjugationPathIntent) -> Unit,
) {
    val stage = pathStage.stage

    StudyBuddyCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (pathStage.isUnlocked) 1f else 0.55f)
            .animateContentSize(),
        onClick = { onIntent(ConjugationPathIntent.ToggleStage(stage.id)) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StageBadge(pathStage)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(stageTitleRes(stage.order)),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(CoreUiR.string.conjugation_stage_verb, stage.verb.infinitive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(
                        CoreUiR.string.conjugation_steps_done,
                        pathStage.completedStepCount,
                        ConjugationStep.entries.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            QuestCreature(characterId = stage.friendCharacterId, size = 56.dp)
        }

        if (isExpanded) {
            StageDetails(pathStage = pathStage, onIntent = onIntent)
        }
    }
}

@Composable
private fun StageBadge(pathStage: ConjugationPathStage) {
    val color = when {
        pathStage.isCompleted -> MaterialTheme.colorScheme.tertiary
        pathStage.isUnlocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        when {
            pathStage.isCompleted -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
            )

            !pathStage.isUnlocked -> Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(CoreUiR.string.conjugation_stage_locked),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> Text(
                text = "${pathStage.stage.order}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun StageDetails(
    pathStage: ConjugationPathStage,
    onIntent: (ConjugationPathIntent) -> Unit,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(
                if (pathStage.isUnlocked) {
                    stageStoryRes(pathStage.stage.order)
                } else {
                    CoreUiR.string.conjugation_stage_locked
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConjugationStep.entries.forEach { step ->
                StepChip(
                    pathStage = pathStage,
                    step = step,
                    onClick = { onIntent(ConjugationPathIntent.OpenStep(pathStage.stage.id, step)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StepChip(
    pathStage: ConjugationPathStage,
    step: ConjugationStep,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = pathStage.stepProgress[step]?.isCompleted == true
    val isUnlocked = pathStage.isStepUnlocked(step)
    val label = stringResource(stepLabelRes(step))
    val background = when {
        isDone -> MaterialTheme.colorScheme.tertiaryContainer
        isUnlocked -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .alpha(if (isUnlocked) 1f else 0.5f)
            .accessibleClickable(label = label, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = when {
                isDone -> Icons.Filled.Check
                isUnlocked -> Icons.Filled.PlayArrow
                else -> Icons.Filled.Lock
            },
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConjugationPathPreview() {
    StudyBuddyTheme {
        ConjugationPathContent(
            state = ConjugationPathState(isLoading = true),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
