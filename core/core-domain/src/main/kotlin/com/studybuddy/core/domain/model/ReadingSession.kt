package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class ReadingSession(
    val id: String,
    val profileId: String,
    val poemId: String,
    val score: Float,
    val accuracyPct: Float,
    val durationSeconds: Int,
    val language: String,
    val createdAt: Instant,
)
