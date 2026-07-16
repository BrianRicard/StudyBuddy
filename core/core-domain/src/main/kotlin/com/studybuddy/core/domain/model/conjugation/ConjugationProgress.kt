package com.studybuddy.core.domain.model.conjugation

import kotlinx.datetime.Instant

/**
 * Best result for one step of one stage. One record per (profile, stage, step).
 *
 * @property bestCorrect Highest number of correct answers achieved in the step.
 * @property bestTotal The total answer count of that best run.
 * @property completedAt When the step was first completed (null = not yet).
 */
data class ConjugationProgress(
    val id: String,
    val profileId: String,
    val stageId: String,
    val step: ConjugationStep,
    val bestCorrect: Int,
    val bestTotal: Int,
    val completedAt: Instant?,
    val updatedAt: Instant,
) {
    val isCompleted: Boolean get() = completedAt != null
    val isPerfect: Boolean get() = isCompleted && bestTotal > 0 && bestCorrect == bestTotal
}
