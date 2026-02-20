package com.studybuddy.core.data.backup

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor() {
    suspend fun generateReport(profileId: String): ByteArray {
        // TODO: Use Android PdfDocument API to render progress report
        return byteArrayOf()
    }
}
