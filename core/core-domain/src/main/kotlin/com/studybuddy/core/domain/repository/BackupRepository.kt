package com.studybuddy.core.domain.repository

interface BackupRepository {
    suspend fun createBackup(): String
    suspend fun restoreBackup(json: String)
    suspend fun exportPdf(profileId: String): ByteArray
    suspend fun exportCsv(profileId: String): String
    suspend fun sync()
}
