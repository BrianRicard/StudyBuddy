package com.studybuddy.shared.ink

data class DownloadProgress(
    val languageTag: String,
    val isComplete: Boolean = false,
    val error: String? = null,
)
