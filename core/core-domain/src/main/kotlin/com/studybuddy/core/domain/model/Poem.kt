package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val lines: List<String>,
    val language: String,
    val source: PoemSource,
    val isFavourite: Boolean = false,
    val tags: List<String> = emptyList(),
    val cachedAt: Instant? = null,
)

enum class PoemSource {
    API,
    BUNDLED,
    USER,
}
