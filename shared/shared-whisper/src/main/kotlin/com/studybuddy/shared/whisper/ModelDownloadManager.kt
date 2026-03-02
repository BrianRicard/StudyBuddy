package com.studybuddy.shared.whisper

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages downloading and caching whisper GGML model files.
 *
 * Models are stored in `context.filesDir/whisper_models/` and persist across app restarts.
 */
@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val modelsDir: File
        get() = File(context.filesDir, "whisper_models").also { it.mkdirs() }

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 600_000L // 10 minutes for large models
            connectTimeoutMillis = 30_000L
        }
    }

    /**
     * Returns the local file path if the model is already downloaded, null otherwise.
     */
    fun getModelPath(model: WhisperModel): String? {
        val file = File(modelsDir, model.fileName)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    /**
     * Download a model file with progress reporting.
     *
     * @param model The model variant to download.
     * @param onProgress Progress callback with value in [0.0, 1.0].
     * @return The local file path on success.
     */
    suspend fun downloadModel(
        model: WhisperModel,
        onProgress: (Float) -> Unit = {},
    ): Result<String> = withContext(Dispatchers.IO) {
        val targetFile = File(modelsDir, model.fileName)
        val tempFile = File(modelsDir, "${model.fileName}.tmp")

        try {
            val expectedSize = model.sizeMb * 1_048_576L // Approximate

            client.prepareGet(model.downloadUrl).execute { response ->
                val channel = response.bodyAsChannel()
                val outputStream = tempFile.outputStream()
                var totalRead = 0L
                val buffer = ByteArray(8192)

                outputStream.use { out ->
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer)
                        if (read > 0) {
                            out.write(buffer, 0, read)
                            totalRead += read
                            onProgress((totalRead.toFloat() / expectedSize).coerceAtMost(0.99f))
                        }
                    }
                }
            }

            tempFile.renameTo(targetFile)
            onProgress(1.0f)
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(e)
        }
    }

    /**
     * Delete a cached model file.
     */
    fun deleteModel(model: WhisperModel) {
        File(modelsDir, model.fileName).delete()
    }
}
