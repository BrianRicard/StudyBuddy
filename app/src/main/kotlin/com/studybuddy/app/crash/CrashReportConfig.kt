package com.studybuddy.app.crash

import android.content.Context
import com.studybuddy.app.BuildConfig

object CrashReportConfig {
    const val REPO_OWNER = "BrianRicard"
    const val REPO_NAME = "StudyBuddy"

    fun getToken(@Suppress("UNUSED_PARAMETER") context: Context): String {
        return BuildConfig.CRASH_REPORT_TOKEN
    }
}
