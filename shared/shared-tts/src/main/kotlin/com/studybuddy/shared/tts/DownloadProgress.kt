package com.studybuddy.shared.tts

data class DownloadProgress(
    val locale: String,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val isComplete: Boolean = false,
    val error: String? = null,
) {
    val progressPercent: Float
        get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}
