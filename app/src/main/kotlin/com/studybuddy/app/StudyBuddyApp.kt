package com.studybuddy.app

import android.app.Application
import android.content.Context
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat

@HiltAndroidApp
class StudyBuddyApp : Application() {

    @Inject
    lateinit var ttsManager: TtsManager

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        ACRA.init(
            this,
            CoreConfigurationBuilder()
                .withReportFormat(StringFormat.KEY_VALUE_LIST)
                .withReportContent(
                    ReportField.STACK_TRACE,
                    ReportField.APP_VERSION_NAME,
                    ReportField.APP_VERSION_CODE,
                    ReportField.ANDROID_VERSION,
                    ReportField.PHONE_MODEL,
                    ReportField.BRAND,
                    ReportField.AVAILABLE_MEM_SIZE,
                    ReportField.TOTAL_MEM_SIZE,
                    ReportField.LOGCAT,
                    ReportField.CUSTOM_DATA,
                    ReportField.USER_COMMENT,
                    ReportField.USER_CRASH_DATE,
                )
                .withLogcatArguments("-t", "100", "-v", "time")
                .build(),
        )
    }

    override fun onCreate() {
        super.onCreate()
        ttsManager.initialize()
    }
}
