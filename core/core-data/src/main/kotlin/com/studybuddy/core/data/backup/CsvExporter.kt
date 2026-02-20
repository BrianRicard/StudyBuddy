package com.studybuddy.core.data.backup

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {
    suspend fun exportWordLists(profileId: String): String {
        // TODO: Query dictee lists and words, format as CSV
        return ""
    }
}
