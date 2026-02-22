package com.studybuddy.core.domain.usecase.backup

import com.studybuddy.core.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(private val repository: BackupRepository) {
    suspend operator fun invoke(json: String) = repository.restoreBackup(json)
}
