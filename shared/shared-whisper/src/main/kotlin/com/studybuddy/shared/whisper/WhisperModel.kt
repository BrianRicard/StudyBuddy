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
) {
    TINY(
        fileName = "ggml-tiny.bin",
        displayName = "Compact",
        sizeMb = 40,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
    ),
    BASE(
        fileName = "ggml-base.bin",
        displayName = "Standard",
        sizeMb = 140,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
    ),
    SMALL(
        fileName = "ggml-small.bin",
        displayName = "High Quality",
        sizeMb = 460,
        downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
    ),
}
