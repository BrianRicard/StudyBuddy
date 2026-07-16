package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.datetime.Instant

/**
 * Maps a row to the domain model, or null when the step name is unknown
 * (e.g. data written by a newer app version) — one bad row must not kill
 * the whole progress flow.
 */
fun ConjugationProgressEntity.toDomainOrNull(): ConjugationProgress? {
    val parsedStep = runCatching { ConjugationStep.valueOf(step) }.getOrNull() ?: return null
    return ConjugationProgress(
        id = id,
        profileId = profileId,
        stageId = stageId,
        step = parsedStep,
        bestCorrect = bestCorrect,
        bestTotal = bestTotal,
        completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )
}

fun ConjugationProgress.toEntity() = ConjugationProgressEntity(
    id = id,
    profileId = profileId,
    stageId = stageId,
    step = step.name,
    bestCorrect = bestCorrect,
    bestTotal = bestTotal,
    completedAt = completedAt?.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
)
