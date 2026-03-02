package com.studybuddy.shared.whisper

/**
 * Low-level JNI declarations for whisper.cpp native library.
 *
 * This object loads the native shared library and exposes the C functions
 * required for model loading, transcription, and context management.
 */
object WhisperJni {

    init {
        System.loadLibrary("whisper_jni")
    }

    /**
     * Initialize a whisper context from a model file path.
     * @return Native context pointer, or 0 on failure.
     */
    external fun initContext(modelPath: String): Long

    /**
     * Free a previously initialized whisper context.
     */
    external fun freeContext(context: Long)

    /**
     * Run full transcription on PCM audio samples.
     *
     * @param context Native context pointer from [initContext].
     * @param samples Float array of PCM audio samples normalized to [-1, 1].
     * @param numSamples Number of samples in the array.
     * @param language ISO language code (e.g. "en", "fr", "de").
     * @param initialPrompt Reference text to bias the decoder (the poem text).
     * @param numThreads Number of CPU threads to use.
     * @return JSON string with format: {"text":"...", "segments":[{"text":"...", "t0":0, "t1":100}]}
     */
    external fun fullTranscribe(
        context: Long,
        samples: FloatArray,
        numSamples: Int,
        language: String,
        initialPrompt: String,
        numThreads: Int,
    ): String
}
