package com.studybuddy.feature.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studybuddy.core.domain.usecase.backup.CreateBackupUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val createBackupUseCase: CreateBackupUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            createBackupUseCase()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
