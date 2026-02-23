plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.android.room")
    id("studybuddy.jvm.test")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.studybuddy.core.data"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.work.runtime.ktx)

    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.ext.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
}
