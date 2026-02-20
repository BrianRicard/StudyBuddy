package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class DicteeWord(
    val id: String,
    val listId: String,
    val word: String,
    val mastered: Boolean = false,
    val attempts: Int = 0,
    val correctCount: Int = 0,
    val lastAttemptAt: Instant? = null,
)
