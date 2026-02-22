package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class Profile(
    val id: String,
    val name: String,
    val avatarConfig: AvatarConfig = AvatarConfig.default(),
    val locale: String = "en",
    val totalPoints: Long = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
