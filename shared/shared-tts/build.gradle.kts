plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.shared.tts"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(libs.coroutines.android)
}
