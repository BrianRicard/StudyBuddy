package com.studybuddy.app

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyBuddyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initCrashlytics()
    }

    private fun initCrashlytics() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        } catch (_: IllegalStateException) {
            // Firebase not configured (no google-services.json) — skip
        }
    }
}
