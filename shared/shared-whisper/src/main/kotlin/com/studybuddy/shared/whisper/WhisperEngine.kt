package com.studybuddy.shared.whisper

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A single transcription segment with timing info.
 */
@Serializable
data class Segment(
    val text: String,
    val t0: Long = 0,
    val t1: Long = 0,
)

/**
 * Result of a whisper transcription.
 */
@Serializable
data class TranscriptionResult(
    val text: String,
    val segments: List<Segment> = emptyList(),
)

/**
 * Kotlin wrapper around the whisper.cpp JNI bridge.
 *
 * Manages the native context lifecycle and provides a coroutine-friendly
 * transcription API. Thread-safe via mutex — only one transcription runs at a time.
 */
@Singleton
class WhisperEngine @Inject constructor() {

    private var contextPtr: Long = 0L
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val isInitialized: Boolean get() = contextPtr != 0L

    /**
     * Load a whisper model from the given file path.
     * Must be called before [transcribe]. Safe to call multiple times — frees previous context.
     */
    suspend fun initialize(modelPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (contextPtr != 0L) {
                WhisperJni.freeContext(contextPtr)
                contextPtr = 0L
            }
            val ptr = WhisperJni.initContext(modelPath)
            if (ptr == 0L) {
                Result.failure(IllegalStateException("Failed to load whisper model: $modelPath"))
            } else {
                contextPtr = ptr
                Result.success(Unit)
            }
        }
    }

    /**
     * Transcribe PCM audio samples.
     *
     * @param samples Float array normalized to [-1, 1], recorded at 16kHz mono.
     * @param language ISO language code ("en", "fr", "de").
     * @param initialPrompt Reference text (poem) to bias the decoder.
     * @return Parsed [TranscriptionResult] or failure.
     */
    suspend fun transcribe(
        samples: FloatArray,
        language: String,
        initialPrompt: String,
    ): Result<TranscriptionResult> = withContext(Dispatchers.Default) {
        mutex.withLock {
            if (contextPtr == 0L) {
                return@withContext Result.failure(
                    IllegalStateException("WhisperEngine not initialized — call initialize() first"),
                )
            }

            val numThreads = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)

            val jsonStr = WhisperJni.fullTranscribe(
                context = contextPtr,
                samples = samples,
                numSamples = samples.size,
                language = language,
                initialPrompt = initialPrompt,
                numThreads = numThreads,
            )

            try {
                val result = json.decodeFromString<TranscriptionResult>(jsonStr)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Free native resources. Safe to call multiple times.
     */
    fun release() {
        if (contextPtr != 0L) {
            WhisperJni.freeContext(contextPtr)
            contextPtr = 0L
        }
    }
}
