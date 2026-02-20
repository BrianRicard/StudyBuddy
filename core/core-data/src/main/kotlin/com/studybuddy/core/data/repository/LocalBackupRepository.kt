package com.studybuddy.core.data.repository

import com.studybuddy.core.domain.repository.BackupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupRepository @Inject constructor() : BackupRepository {

    override suspend fun createBackup(): String {
        // TODO: Implement full DB serialization to JSON
        return "{}"
    }

    override suspend fun restoreBackup(json: String) {
        // TODO: Implement backup restoration
    }

    override suspend fun exportPdf(profileId: String): ByteArray {
        // TODO: Implement PDF generation
        return byteArrayOf()
    }

    override suspend fun exportCsv(profileId: String): String {
        // TODO: Implement CSV export
        return ""
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
