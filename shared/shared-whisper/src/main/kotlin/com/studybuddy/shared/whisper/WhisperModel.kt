package com.studybuddy.shared.whisper

/**
 * Available whisper.cpp GGML model variants.
 *
 * Uses non-`.en` variants for multilingual support (FR + EN + DE).
 * Models are downloaded on-demand and cached in app-internal storage.
 */
enum class WhisperModel(
    val fileName: String,
    val displayName: String,
    val sizeMb: Int,
    val downloadUrl: String,
    val description: String,
) {
    TINY(
        fileName = "ggml-tiny.bin",
        displayName = "Compact",
        sizeMb = 40,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
        description = "Fastest, lower accuracy. Good for older devices with limited storage.",
    ),
    BASE(
        fileName = "ggml-base.bin",
        displayName = "Standard",
        sizeMb = 140,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
        description = "Balanced speed and accuracy. Reasonable for most devices.",
    ),
    SMALL(
        fileName = "ggml-small.bin",
        displayName = "High Quality (Recommended)",
        sizeMb = 460,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
        description = "Best accuracy for children's voices. Recommended if you have the storage space.",
    ),
    ;

    companion object {
        val DEFAULT = SMALL

        fun fromFileName(fileName: String): WhisperModel? = entries.find { it.fileName == fileName }
    }
}
