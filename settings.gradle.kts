pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
