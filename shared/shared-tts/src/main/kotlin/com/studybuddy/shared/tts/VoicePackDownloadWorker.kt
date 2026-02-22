package com.studybuddy.shared.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VoicePackDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val locale = inputData.getString(KEY_LOCALE) ?: return Result.failure()

        return try {
            // Trigger TTS data installation via system intent
            val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(installIntent)

            Result.success(workDataOf(KEY_LOCALE to locale))
        } catch (e: Exception) {
            Result.failure(workDataOf(KEY_ERROR to e.message))
        }
    }

    companion object {
        const val KEY_LOCALE = "locale"
        const val KEY_ERROR = "error"
        const val WORK_NAME = "voice_pack_download"
    }
}
