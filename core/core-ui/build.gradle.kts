plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.compose")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.core.ui"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(libs.navigation.compose)
    implementation(libs.lottie.compose)
    api(libs.compose.material3)
    implementation(libs.bundles.lifecycle)
}
