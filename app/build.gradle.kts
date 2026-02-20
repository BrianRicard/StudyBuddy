plugins {
    id("studybuddy.android.application")
    id("studybuddy.android.compose")
    id("studybuddy.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.studybuddy.app"

    defaultConfig {
        applicationId = "com.studybuddy.app"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))

    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-dictee"))
    implementation(project(":feature:feature-math"))
    implementation(project(":feature:feature-avatar"))
    implementation(project(":feature:feature-rewards"))
    implementation(project(":feature:feature-stats"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-backup"))
    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-poems"))

    implementation(project(":shared:shared-points"))
    implementation(project(":shared:shared-tts"))
    implementation(project(":shared:shared-ink"))

    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.core.ktx)

    debugImplementation(libs.leakcanary)
}
