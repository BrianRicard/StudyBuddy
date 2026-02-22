package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.MathSessionEntity
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.model.Operator
import kotlinx.datetime.Instant

fun MathSessionEntity.toDomain() = MathSession(
    id = id,
    profileId = profileId,
    operators = operators.split(",").mapNotNull { name ->
        runCatching { Operator.valueOf(name.trim()) }.getOrNull()
    }.toSet(),
    numberRange = rangeMin..rangeMax,
    totalProblems = totalProblems,
    correctCount = correctCount,
    bestStreak = bestStreak,
    avgResponseMs = avgResponseMs,
    difficulty = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.MEDIUM),
    completedAt = Instant.fromEpochMilliseconds(completedAt),
)

fun MathSession.toEntity() = MathSessionEntity(
    id = id,
    profileId = profileId,
    operators = operators.joinToString(",") { it.name },
    rangeMin = numberRange.first,
    rangeMax = numberRange.last,
    totalProblems = totalProblems,
    correctCount = correctCount,
    bestStreak = bestStreak,
    avgResponseMs = avgResponseMs,
    difficulty = difficulty.name,
    completedAt = completedAt.toEpochMilliseconds(),
)
