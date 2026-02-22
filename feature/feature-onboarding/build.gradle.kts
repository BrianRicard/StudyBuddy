plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.onboarding"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":shared:shared-tts"))
}
