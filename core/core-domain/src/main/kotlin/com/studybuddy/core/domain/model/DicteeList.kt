package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class DicteeList(
    val id: String,
    val profileId: String,
    val title: String,
    val language: String,
    val wordCount: Int = 0,
    val masteredCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
