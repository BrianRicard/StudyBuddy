pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application", "com.android.library" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
                "org.jetbrains.kotlin.android" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.plugin.compose" ->
                    useModule("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.plugin.serialization" ->
                    useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
                "com.google.dagger.hilt.android" ->
                    useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
                "com.google.devtools.ksp" ->
                    useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
                "androidx.room" ->
                    useModule("androidx.room:room-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlinx.kover" ->
                    useModule("org.jetbrains.kotlinx:kover-gradle-plugin:${requested.version}")
                "io.gitlab.arturbosch.detekt" ->
                    useModule("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${requested.version}")
                "org.jlleitschuh.gradle.ktlint" ->
                    useModule("org.jlleitschuh.gradle:ktlint-gradle:${requested.version}")
                "com.github.ben-manes.versions" ->
                    useModule("com.github.ben-manes:gradle-versions-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StudyBuddy"

include(":app")

// Core
include(":core:core-ui")
include(":core:core-data")
include(":core:core-domain")
include(":core:core-common")

// Features
include(":feature:feature-home")
include(":feature:feature-dictee")
include(":feature:feature-math")
include(":feature:feature-avatar")
include(":feature:feature-rewards")
include(":feature:feature-stats")
include(":feature:feature-settings")
include(":feature:feature-backup")
include(":feature:feature-onboarding")
include(":feature:feature-poems")

// Shared
include(":shared:shared-points")
include(":shared:shared-tts")
include(":shared:shared-ink")
