plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.poems"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":shared:shared-points"))
    implementation(project(":shared:shared-tts"))
    implementation(project(":shared:shared-whisper"))
}
