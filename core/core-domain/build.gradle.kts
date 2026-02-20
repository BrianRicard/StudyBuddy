plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
}

android {
    namespace = "com.studybuddy.core.domain"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
