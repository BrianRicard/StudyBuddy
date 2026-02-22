plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.core.common"
}

dependencies {
    api(libs.coroutines.core)
    api(libs.kotlinx.datetime)
}
