package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.MathFactReviewEntity
import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import kotlinx.datetime.Instant

fun MathFactReviewEntity.toDomain(): MathFactReview = MathFactReview(
    id = id,
    profileId = profileId,
    table = tableNumber,
    multiplicand = multiplicand,
    box = box,
    dueAt = Instant.fromEpochMilliseconds(dueAt),
    lapses = lapses,
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)

fun MathFactReview.toEntity() = MathFactReviewEntity(
    id = id,
    profileId = profileId,
    tableNumber = table,
    multiplicand = multiplicand,
    box = box,
    dueAt = dueAt.toEpochMilliseconds(),
    lapses = lapses,
    updatedAt = updatedAt.toEpochMilliseconds(),
)
