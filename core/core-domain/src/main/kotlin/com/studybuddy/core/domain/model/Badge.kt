package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedAt: Instant? = null,
    val isEarned: Boolean = false,
)
