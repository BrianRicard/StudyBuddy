plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.compose")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.shared.points"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(libs.bundles.lifecycle)
}
