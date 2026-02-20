plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.stats"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
}
