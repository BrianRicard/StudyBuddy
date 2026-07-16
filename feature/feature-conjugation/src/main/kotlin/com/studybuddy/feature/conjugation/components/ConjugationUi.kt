package com.studybuddy.feature.conjugation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.avatar.AvatarCharacterRegistry
import com.studybuddy.core.ui.avatar.CreatureCanvas
import com.studybuddy.core.domain.model.conjugation.ConjugationStep

/** Title of a quest stage, keyed by its order on the path. */
@StringRes
fun stageTitleRes(order: Int): Int = when (order) {
    1 -> CoreUiR.string.conjugation_stage_title_1
    2 -> CoreUiR.string.conjugation_stage_title_2
    3 -> CoreUiR.string.conjugation_stage_title_3
    4 -> CoreUiR.string.conjugation_stage_title_4
    5 -> CoreUiR.string.conjugation_stage_title_5
    else -> CoreUiR.string.conjugation_stage_title_6
}

/** Story beat shown when a quest stage is opened. */
@StringRes
fun stageStoryRes(order: Int): Int = when (order) {
    1 -> CoreUiR.string.conjugation_stage_story_1
    2 -> CoreUiR.string.conjugation_stage_story_2
    3 -> CoreUiR.string.conjugation_stage_story_3
    4 -> CoreUiR.string.conjugation_stage_story_4
    5 -> CoreUiR.string.conjugation_stage_story_5
    else -> CoreUiR.string.conjugation_stage_story_6
}

/** Short label of a step chip on the path. */
@StringRes
fun stepLabelRes(step: ConjugationStep): Int = when (step) {
    ConjugationStep.LEARN -> CoreUiR.string.conjugation_step_learn
    ConjugationStep.WRITE -> CoreUiR.string.conjugation_step_write
    ConjugationStep.SPEAK -> CoreUiR.string.conjugation_step_speak
    ConjugationStep.BATTLE -> CoreUiR.string.conjugation_step_battle
    ConjugationStep.BOSS -> CoreUiR.string.conjugation_step_boss
}

/** Rotating praise so correct answers never feel repetitive. */
@StringRes
fun praiseRes(seed: Int): Int = when (seed % 3) {
    0 -> CoreUiR.string.conjugation_correct_1
    1 -> CoreUiR.string.conjugation_correct_2
    else -> CoreUiR.string.conjugation_correct_3
}

/** A quest creature (friend or boss) drawn with the shared canvas renderer. */
@Composable
fun QuestCreature(
    characterId: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    CreatureCanvas(
        spec = AvatarCharacterRegistry.getSpec(characterId),
        modifier = modifier,
        size = size,
    )
}

/** "3 / 6" style progress header used by the in-stage game screens. */
@Composable
fun StepProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else current.toFloat() / total },
            modifier = Modifier
                .weight(1f)
                .size(height = 10.dp, width = 0.dp),
        )
        Text(
            text = "$current / $total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
