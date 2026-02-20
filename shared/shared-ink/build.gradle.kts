plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.shared.ink"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.mlkit.ink.recognition)
    implementation(libs.coroutines.android)
}
