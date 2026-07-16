package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.datetime.Instant

fun ConjugationProgressEntity.toDomain() = ConjugationProgress(
    id = id,
    profileId = profileId,
    stageId = stageId,
    step = ConjugationStep.valueOf(step),
    bestCorrect = bestCorrect,
    bestTotal = bestTotal,
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)

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
