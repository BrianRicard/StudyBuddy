plugins {
    id("studybuddy.android.feature")
}

android {
    namespace = "com.studybuddy.feature.math"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":shared:shared-points"))
}
