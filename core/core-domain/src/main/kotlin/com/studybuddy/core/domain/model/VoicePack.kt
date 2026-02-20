package com.studybuddy.core.domain.model

data class VoicePack(
    val id: String,
    val locale: String,
    val displayName: String,
    val sizeBytes: Long,
    val status: VoicePackStatus,
)

enum class VoicePackStatus {
    NOT_INSTALLED,
    DOWNLOADING,
    INSTALLED,
    FAILED,
}
