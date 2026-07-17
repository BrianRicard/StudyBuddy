package com.studybuddy.core.domain.model.conjugation

import kotlinx.datetime.Instant

/**
 * Best result for one step of one stage. One record per (profile, stage, step).
 *
 * @property bestCorrect Number of correct answers in the child's best run.
 * @property bestTotal The total answer count of that best run.
 * @property completedAt When the step was first finished. Any finished run
 * completes a step (never-punishing design); records only exist for finished
 * runs, so this is null only for defensively handled legacy rows.
 * @property updatedAt When the best score last improved.
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
