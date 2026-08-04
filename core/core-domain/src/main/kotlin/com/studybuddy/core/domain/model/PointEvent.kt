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
    CONJUGATION,
    DAILY_LOGIN,
    CHALLENGE,
    PURCHASE,

    /** Stars the parent handed over, e.g. for something done away from the app. */
    GIFT,

    /** Finishing every task the parent set for the day. Paid at most once per day. */
    PLAN_BONUS,

    /** Stars the parent took back in exchange for a real-world reward. Always negative. */
    REDEMPTION,
}
