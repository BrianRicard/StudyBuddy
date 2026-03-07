plugins {
    id("studybuddy.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.studybuddy.feature.reading"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":shared:shared-points"))
    implementation(project(":shared:shared-tts"))
    implementation(libs.kotlinx.serialization.json)
}
