package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class MathSession(
    val id: String,
    val profileId: String,
    val operators: Set<Operator>,
    val numberRange: IntRange,
    val totalProblems: Int,
    val correctCount: Int,
    val bestStreak: Int,
    val avgResponseMs: Long,
    val difficulty: Difficulty,
    val completedAt: Instant,
)
