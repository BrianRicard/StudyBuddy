package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class PointEvent(
    val id: String,
    val profileId: String,
    val source: PointSource,
    val points: Int,
    val reason: String,
    val timestamp: Instant,
)

enum class PointSource {
    DICTEE,
    MATH,
    POEMS,
    READING,
    DAILY_LOGIN,
    CHALLENGE,
    PURCHASE,
    GIFT,
}
