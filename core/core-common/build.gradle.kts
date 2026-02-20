plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.core.common"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.datetime)
}
