plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.backup"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-ui"))
}
