package com.studybuddy.core.domain.usecase.backup

import com.studybuddy.core.domain.repository.BackupRepository
import javax.inject.Inject

class ExportProgressReportUseCase @Inject constructor(private val repository: BackupRepository) {
    suspend fun exportPdf(profileId: String): ByteArray = repository.exportPdf(profileId)

    suspend fun exportCsv(profileId: String): String = repository.exportCsv(profileId)
}
