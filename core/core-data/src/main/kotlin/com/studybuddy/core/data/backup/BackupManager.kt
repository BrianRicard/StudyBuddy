package com.studybuddy.core.data.backup

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor() {
    suspend fun createFullBackup(): String {
        // TODO: Serialize full DB to JSON with schema version
        return "{\"version\": 1}"
    }

    suspend fun restoreFromBackup(json: String) {
        // TODO: Parse JSON, validate schema, write to Room
    }
}
