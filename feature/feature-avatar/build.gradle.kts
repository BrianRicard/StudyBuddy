plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.avatar"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
}
