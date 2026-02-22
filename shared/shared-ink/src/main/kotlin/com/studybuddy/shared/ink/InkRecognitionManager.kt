package com.studybuddy.shared.ink

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class InkRecognitionManager @Inject constructor() {

    private var recognizer: DigitalInkRecognizer? = null
    private var currentLanguageTag: String? = null
    private val modelManager = RemoteModelManager.getInstance()

    fun initialize(languageTag: String) {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
            ?: return
        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model).build(),
        )
        currentLanguageTag = languageTag
    }

    suspend fun recognize(ink: Ink): Result<String> {
        val activeRecognizer = recognizer
            ?: return Result.failure(IllegalStateException("Recognizer not initialized"))
        return suspendCancellableCoroutine { continuation ->
            activeRecognizer.recognize(ink)
                .addOnSuccessListener { result ->
                    val text = result.candidates.firstOrNull()?.text ?: ""
                    continuation.resume(Result.success(text))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }

    suspend fun isModelDownloaded(languageTag: String): Boolean {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
            ?: return false
        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        return suspendCancellableCoroutine { continuation ->
            modelManager.isModelDownloaded(model)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(false) }
        }
    }

    fun downloadModel(languageTag: String): Flow<DownloadProgress> =
        flow {
            emit(DownloadProgress(languageTag = languageTag))
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
            if (modelIdentifier == null) {
                emit(DownloadProgress(languageTag = languageTag, error = "Unsupported language: $languageTag"))
                return@flow
            }
            val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            val result = suspendCancellableCoroutine { continuation ->
                modelManager.download(model, DownloadConditions.Builder().build())
                    .addOnSuccessListener { continuation.resume(true) }
                    .addOnFailureListener { continuation.resume(false) }
            }
            if (result) {
                emit(DownloadProgress(languageTag = languageTag, isComplete = true))
            } else {
                emit(DownloadProgress(languageTag = languageTag, error = "Download failed"))
            }
        }

    fun release() {
        recognizer?.close()
        recognizer = null
        currentLanguageTag = null
    }
}
