package com.studybuddy.app

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StudyBuddyApp : Application() {

    @Inject
    lateinit var ttsManager: TtsManager

    override fun onCreate() {
        super.onCreate()
        initCrashlytics()
        ttsManager.initialize()
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
