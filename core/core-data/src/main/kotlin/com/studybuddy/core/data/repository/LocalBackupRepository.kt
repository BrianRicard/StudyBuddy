package com.studybuddy.core.data.repository

import com.studybuddy.core.data.backup.BackupManager
import com.studybuddy.core.data.backup.CsvExporter
import com.studybuddy.core.data.backup.PdfReportGenerator
import com.studybuddy.core.domain.repository.BackupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupRepository @Inject constructor(
    private val backupManager: BackupManager,
    private val csvExporter: CsvExporter,
    private val pdfReportGenerator: PdfReportGenerator,
) : BackupRepository {

    override suspend fun createBackup(): String =
        backupManager.createFullBackup()

    override suspend fun restoreBackup(json: String) =
        backupManager.restoreFromBackup(json)

    override suspend fun exportPdf(profileId: String): ByteArray =
        pdfReportGenerator.generateReport(profileId)

    override suspend fun exportCsv(profileId: String): String =
        csvExporter.exportWordLists(profileId)

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
