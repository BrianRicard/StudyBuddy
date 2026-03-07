plugins {
    id("studybuddy.android.application")
    id("studybuddy.android.compose")
    id("studybuddy.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.licensee)
}

android {
    namespace = "com.studybuddy.app"

    defaultConfig {
        applicationId = "com.studybuddy.app"

        val crashToken = System.getenv("GITHUB_CRASH_TOKEN")
            ?: providers.gradleProperty("GITHUB_CRASH_TOKEN").getOrElse("")
        buildConfigField("String", "GITHUB_CRASH_TOKEN", "\"$crashToken\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.appcompat)
    implementation(libs.navigation.compose)
    implementation(libs.core.ktx)
    implementation(libs.material3.adaptive.navigation.suite)

    implementation(libs.acra.core)

    debugImplementation(libs.leakcanary)
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allow("ISC")
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://developers.google.com/ml-kit/terms")
    allowUrl("https://developers.google.com/android/licenses")
    ignoreDependencies("org.jetbrains", "annotations")
    ignoreDependencies("org.jetbrains.kotlin")
    ignoreDependencies("org.jetbrains.kotlinx")
}
