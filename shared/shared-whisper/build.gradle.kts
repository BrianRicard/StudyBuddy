plugins {
    id("studybuddy.android.library")
    id("studybuddy.android.hilt")
    id("studybuddy.jvm.test")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.studybuddy.shared.whisper"

    ndkVersion = "27.0.12077973"

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.coroutines.android)
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)
}
