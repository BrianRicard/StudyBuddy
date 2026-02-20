# StudyBuddy — AI Agent Build Instructions

> Complete step-by-step instructions for an AI coding agent to build the StudyBuddy Android app from scratch.
> Target: Production-ready, multi-module Kotlin/Compose Android application with GitHub CI/CD.

---

## Table of Contents

1. [Repository & Environment Setup](#1-repository--environment-setup)
2. [GitHub Actions Pipelines](#2-github-actions-pipelines)
3. [Project Scaffolding](#3-project-scaffolding)
4. [Core Module Implementation](#4-core-module-implementation)
5. [Feature: Home](#5-feature-home)
6. [Feature: Dictée](#6-feature-dictée)
7. [Feature: Speed Math](#7-feature-speed-math)
8. [Feature: Avatar System](#8-feature-avatar-system)
9. [Feature: Rewards Shop](#9-feature-rewards-shop)
10. [Feature: Stats & Progress](#10-feature-stats--progress)
11. [Feature: Settings](#11-feature-settings)
12. [Feature: Backup & Export](#12-feature-backup--export)
13. [Feature: Offline TTS](#13-feature-offline-tts)
14. [Feature: Onboarding](#14-feature-onboarding)
15. [Testing Strategy](#15-testing-strategy)
16. [Release & Signing](#16-release--signing)
17. [Cloud Migration Hooks](#17-cloud-migration-hooks)
18. [Appendix: Coding Standards](#18-appendix-coding-standards)

---

## 1. Repository & Environment Setup

### 1.1 Create the GitHub Repository

```bash
# Create repo
gh repo create study-buddy-android \
  --private \
  --description "StudyBuddy: A fun study aid for kids — Dictée, Speed Math, and more" \
  --clone

cd study-buddy-android

# Initialize with .gitignore
curl -sL https://raw.githubusercontent.com/github/gitignore/main/Android.gitignore > .gitignore

# Add extras to .gitignore
cat >> .gitignore << 'EOF'

# Signing
*.jks
*.keystore
keystore.properties
signing/

# Local secrets
local.properties
secrets.properties

# IDE
.idea/
*.iml

# Build
/build
**/build
.gradle/
EOF
```

### 1.2 Branch Strategy

```
main              ← production releases, protected
├── develop       ← integration branch, PR target
│   ├── feature/* ← feature branches
│   ├── fix/*     ← bug fixes
│   └── chore/*   ← maintenance, CI, deps
└── release/*     ← release candidates
```

Configure branch protection on GitHub:

```bash
# Protect main: require PR, 1 approval, passing CI
gh api repos/{owner}/study-buddy-android/branches/main/protection \
  --method PUT \
  --field required_pull_request_reviews='{"required_approving_review_count":1}' \
  --field required_status_checks='{"strict":true,"contexts":["build","unit-tests","lint"]}' \
  --field enforce_admins=true

# Create develop branch
git checkout -b develop
git push -u origin develop
```

### 1.3 Repository Labels

```bash
# Create issue labels for project management
for label in \
  "module:core:0E8A16" \
  "module:home:1D76DB" \
  "module:dictee:D93F0B" \
  "module:math:FBCA04" \
  "module:avatar:C2E0C6" \
  "module:rewards:D4C5F9" \
  "module:stats:F9D0C4" \
  "module:settings:BFD4F2" \
  "module:backup:EDEDED" \
  "module:tts:B60205" \
  "module:onboarding:FEF2C0" \
  "priority:critical:B60205" \
  "priority:high:D93F0B" \
  "priority:medium:FBCA04" \
  "priority:low:0E8A16" \
  "type:feature:1D76DB" \
  "type:bug:B60205" \
  "type:chore:EDEDED" \
  "type:test:D4C5F9"; do
  IFS=':' read -r name1 name2 color <<< "$label"
  gh label create "${name1}:${name2}" --color "$color" 2>/dev/null || true
done
```

### 1.4 GitHub Repository Secrets

Configure these secrets in GitHub → Settings → Secrets and variables → Actions:

| Secret Name | Purpose | When Needed |
|---|---|---|
| `KEYSTORE_BASE64` | Release signing keystore (base64-encoded) | Release builds |
| `KEYSTORE_PASSWORD` | Keystore password | Release builds |
| `KEY_ALIAS` | Key alias | Release builds |
| `KEY_PASSWORD` | Key password | Release builds |
| `FIREBASE_APP_ID` | Firebase project (crashlytics) | Optional, Phase 4+ |

---

## 2. GitHub Actions Pipelines

### 2.1 CI Pipeline — Build & Test

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'
  GRADLE_OPTS: '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true'

jobs:
  # ─────────────────────────────────────────────
  # Job 1: Lint & Static Analysis
  # ─────────────────────────────────────────────
  lint:
    name: Lint & Detekt
    runs-on: ubuntu-latest
    timeout-minutes: 15
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/develop' }}

      - name: Run Android Lint
        run: ./gradlew lintDebug --continue

      - name: Run Detekt
        run: ./gradlew detekt --continue

      - name: Run Ktlint
        run: ./gradlew ktlintCheck --continue

      - name: Upload Lint Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-reports
          path: |
            **/build/reports/lint-results-*.html
            **/build/reports/detekt/*.html
          retention-days: 7

  # ─────────────────────────────────────────────
  # Job 2: Unit Tests
  # ─────────────────────────────────────────────
  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/develop' }}

      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest --continue

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-results
          path: '**/build/test-results/testDebugUnitTest/*.xml'
          retention-days: 7

      - name: Test Report Summary
        if: always()
        uses: dorny/test-reporter@v1
        with:
          name: Unit Test Report
          path: '**/build/test-results/testDebugUnitTest/*.xml'
          reporter: java-junit

  # ─────────────────────────────────────────────
  # Job 3: Build Debug APK
  # ─────────────────────────────────────────────
  build:
    name: Build Debug
    runs-on: ubuntu-latest
    timeout-minutes: 25
    needs: [lint, unit-tests]
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/develop' }}

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 14

  # ─────────────────────────────────────────────
  # Job 4: Instrumented Tests (on PR to main only)
  # ─────────────────────────────────────────────
  instrumented-tests:
    name: Instrumented Tests
    runs-on: ubuntu-latest
    timeout-minutes: 45
    if: github.event_name == 'pull_request' && github.base_ref == 'main'
    needs: [build]
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: AVD Cache
        uses: actions/cache@v4
        id: avd-cache
        with:
          path: |
            ~/.android/avd/*
            ~/.android/adb*
          key: avd-api-31-${{ runner.os }}

      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 31
          arch: x86_64
          target: google_apis
          force-avd-creation: false
          emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          disable-animations: true
          script: ./gradlew connectedDebugAndroidTest --continue

      - name: Upload Instrumented Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: instrumented-test-results
          path: '**/build/reports/androidTests/connected/'
          retention-days: 7
```

### 2.2 Release Pipeline

Create `.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'

jobs:
  release-build:
    name: Build Release APK & AAB
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Extract version from tag
        id: version
        run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT

      - name: Decode Keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/release.keystore

      - name: Build Release APK
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease

      - name: Build Release AAB
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease

      - name: Run Release Unit Tests
        run: ./gradlew testReleaseUnitTest

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          name: "StudyBuddy v${{ steps.version.outputs.VERSION }}"
          generate_release_notes: true
          files: |
            app/build/outputs/apk/release/app-release.apk
            app/build/outputs/bundle/release/app-release.aab

      - name: Upload to Play Store (internal track)
        if: false  # Enable when Play Store is configured
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT }}
          packageName: com.studybuddy.app
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
```

### 2.3 Dependency Update Pipeline

Create `.github/workflows/dependencies.yml`:

```yaml
name: Dependency Updates

on:
  schedule:
    - cron: '0 8 * * 1'  # Every Monday at 8 AM UTC
  workflow_dispatch:

jobs:
  dependency-check:
    name: Check Dependency Updates
    runs-on: ubuntu-latest
    timeout-minutes: 15
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Check for dependency updates
        run: ./gradlew dependencyUpdates -Drevision=release

      - name: Upload dependency report
        uses: actions/upload-artifact@v4
        with:
          name: dependency-updates
          path: build/dependencyUpdates/report.txt
          retention-days: 7
```

### 2.4 Code Coverage Pipeline

Create `.github/workflows/coverage.yml`:

```yaml
name: Code Coverage

on:
  push:
    branches: [develop]

jobs:
  coverage:
    name: Generate Coverage Report
    runs-on: ubuntu-latest
    timeout-minutes: 25
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Tests with Coverage
        run: ./gradlew koverXmlReportDebug

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: '**/build/reports/kover/reportDebug.xml'
          flags: unittests
          fail_ci_if_error: false

      - name: Add Coverage PR Comment
        if: github.event_name == 'pull_request'
        uses: madrapps/jacoco-report@v1.7.1
        with:
          paths: '**/build/reports/kover/reportDebug.xml'
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 60
          min-coverage-changed-files: 80
```

---

## 3. Project Scaffolding

### 3.1 Create the Android Project

Use Android Studio or the command line. The following defines the full project structure. Create every file listed.

**Package name:** `com.studybuddy.app`
**Min SDK:** 26 (Android 8.0)
**Target SDK:** 35
**Compile SDK:** 35

### 3.2 Module Structure

```
study-buddy-android/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── release.yml
│       ├── dependencies.yml
│       └── coverage.yml
├── app/                              # App shell module
├── core/
│   ├── core-ui/                      # Theme, design system, shared composables
│   ├── core-data/                    # Room DB, DAOs, repositories
│   ├── core-domain/                  # Domain models, use cases, repository interfaces
│   └── core-common/                  # Extensions, utils, constants
├── feature/
│   ├── feature-home/
│   ├── feature-dictee/
│   ├── feature-math/
│   ├── feature-avatar/
│   ├── feature-rewards/
│   ├── feature-stats/
│   ├── feature-settings/
│   ├── feature-backup/
│   ├── feature-onboarding/
│   └── feature-poems/                # Stub module for future
├── shared/
│   ├── shared-points/                # Points system logic + UI components
│   ├── shared-tts/                   # TextToSpeech wrapper
│   └── shared-ink/                   # ML Kit handwriting wrapper
├── gradle/
│   └── libs.versions.toml            # Version catalog
├── build.gradle.kts                  # Root build file
├── settings.gradle.kts
├── detekt.yml
├── .editorconfig
└── README.md
```

### 3.3 Version Catalog

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
compose-bom = "2024.12.01"
compose-compiler = "1.5.15"
hilt = "2.53.1"
room = "2.6.1"
navigation = "2.8.5"
lifecycle = "2.8.7"
coroutines = "1.9.0"
lottie = "6.6.2"
mlkit-ink = "18.1.0"
junit5 = "5.11.4"
turbine = "1.2.0"
mockk = "1.13.14"
kover = "0.9.0"
detekt = "1.23.7"
ktlint = "12.1.2"
leakcanary = "2.14"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"
datastore = "1.1.1"
work-manager = "2.10.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-runtime = { group = "androidx.compose.runtime", name = "runtime" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinx-datetime" }

# Lottie
lottie-compose = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }

# ML Kit
mlkit-ink-recognition = { group = "com.google.mlkit", name = "digital-ink-recognition", version.ref = "mlkit-ink" }

# WorkManager
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work-manager" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }

# Testing
junit5-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
junit5-params = { group = "org.junit.jupiter", name = "junit-jupiter-params", version.ref = "junit5" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

# Debug
leakcanary = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }

[bundles]
compose = [
  "compose-ui", "compose-ui-tooling-preview", "compose-material3",
  "compose-animation", "compose-foundation", "compose-runtime"
]
lifecycle = ["lifecycle-runtime-compose", "lifecycle-viewmodel-compose"]
room = ["room-runtime", "room-ktx"]
testing = ["junit5-api", "junit5-params", "turbine", "mockk", "coroutines-test"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### 3.4 Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        android.set(true)
        verbose.set(true)
    }
}
```

### 3.5 `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
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
```

### 3.6 Convention Plugin (build-logic)

Create `build-logic/` directory with shared build configurations to reduce duplication across modules.

Create `build-logic/convention/build.gradle.kts`:

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}
```

Create convention plugins for:

| Plugin | ID | Purpose |
|---|---|---|
| `AndroidApplicationConventionPlugin` | `studybuddy.android.application` | App module setup |
| `AndroidLibraryConventionPlugin` | `studybuddy.android.library` | Library modules |
| `AndroidComposeConventionPlugin` | `studybuddy.android.compose` | Compose modules |
| `AndroidFeatureConventionPlugin` | `studybuddy.android.feature` | Feature modules (library + compose + hilt + nav) |
| `AndroidHiltConventionPlugin` | `studybuddy.android.hilt` | Hilt DI |
| `AndroidRoomConventionPlugin` | `studybuddy.android.room` | Room DB |
| `JvmTestConventionPlugin` | `studybuddy.jvm.test` | JUnit 5 testing |

Each feature module's `build.gradle.kts` then becomes minimal:

```kotlin
plugins {
    id("studybuddy.android.feature")
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":shared:shared-points"))
}
```

### 3.7 Commit & Push Scaffold

```bash
git add -A
git commit -m "chore: scaffold project structure with multi-module setup

- Configure Gradle version catalog with all dependencies
- Add build-logic convention plugins
- Create all module directories with build.gradle.kts
- Add GitHub Actions CI/CD pipelines
- Configure Detekt and Ktlint"

git push origin develop
```

---

## 4. Core Module Implementation

### 4.1 core-common

Purpose: Extensions, constants, utilities shared everywhere.

```
core/core-common/src/main/kotlin/com/studybuddy/core/common/
├── extensions/
│   ├── FlowExtensions.kt          # Flow.collectAsStateWithLifecycle helpers
│   ├── StringExtensions.kt        # Accent-aware comparison, normalize()
│   └── DateTimeExtensions.kt      # Kotlinx datetime helpers
├── result/
│   └── Result.kt                  # sealed class: Success<T>, Error(exception), Loading
├── constants/
│   ├── PointValues.kt             # All point constants (DICTEE_CORRECT = 10, etc.)
│   └── AppConstants.kt            # Max streak, daily goal defaults
├── locale/
│   └── SupportedLocale.kt         # enum: FR, EN, DE with java.util.Locale mapping
└── di/
    └── DispatchersModule.kt       # Hilt module providing IO, Default, Main dispatchers
```

**Key implementation: `StringExtensions.kt`**

```kotlin
/**
 * Accent-aware string comparison for dictée grading.
 *
 * @param target The correct word
 * @param strict If true, accents must match exactly. If false, base characters are compared.
 * @return true if the strings match under the given strictness
 */
fun String.matchesWord(target: String, strict: Boolean = false): Boolean {
    if (strict) return this.trim().equals(target.trim(), ignoreCase = true)
    val normalize = { s: String ->
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
            .lowercase()
            .trim()
    }
    return normalize(this) == normalize(target)
}
```

**Key implementation: `PointValues.kt`**

```kotlin
object PointValues {
    // Dictée
    const val DICTEE_CORRECT_TYPED = 10
    const val DICTEE_CORRECT_HANDWRITTEN = 15
    const val DICTEE_PERFECT_LIST = 50

    // Math
    const val MATH_CORRECT = 5
    const val MATH_STREAK_5 = 25
    const val MATH_STREAK_10 = 75
    const val MATH_STREAK_20 = 150

    // General
    const val DAILY_LOGIN = 10
    const val FIRST_SESSION_OF_DAY = 20
    const val DAILY_CHALLENGE_COMPLETE = 100
    const val WEEKLY_CHALLENGE_COMPLETE = 200

    // Streak multipliers
    fun streakMultiplier(streak: Int): Double = when {
        streak < 5 -> 1.0
        streak < 10 -> 1.5
        streak < 20 -> 2.0
        else -> 3.0
    }
}
```

### 4.2 core-domain

Purpose: Domain models, repository interfaces, use cases. No Android dependencies.

```
core/core-domain/src/main/kotlin/com/studybuddy/core/domain/
├── model/
│   ├── Profile.kt
│   ├── DicteeList.kt
│   ├── DicteeWord.kt
│   ├── MathSession.kt
│   ├── MathProblem.kt
│   ├── PointEvent.kt
│   ├── AvatarConfig.kt            # body, hat, face, outfit, pet
│   ├── RewardItem.kt
│   ├── Badge.kt
│   ├── Operator.kt                 # enum: PLUS, MINUS, MULTIPLY, DIVIDE, POWER
│   ├── Difficulty.kt               # enum: EASY, MEDIUM, HARD, ADAPTIVE
│   ├── Feedback.kt                 # sealed: Correct, Incorrect(correctAnswer), TimeUp
│   ├── InputMode.kt                # enum: KEYBOARD, HANDWRITING
│   └── VoicePack.kt               # id, locale, displayName, sizeBytes, status
├── repository/
│   ├── ProfileRepository.kt
│   ├── DicteeRepository.kt
│   ├── MathRepository.kt
│   ├── PointsRepository.kt
│   ├── AvatarRepository.kt
│   ├── RewardsRepository.kt
│   ├── BackupRepository.kt
│   ├── SettingsRepository.kt
│   └── VoicePackRepository.kt
└── usecase/
    ├── dictee/
    │   ├── GetDicteeListsUseCase.kt
    │   ├── AddWordUseCase.kt
    │   ├── CheckSpellingUseCase.kt
    │   └── GetPracticeWordsUseCase.kt     # Random order, weighted by mastery
    ├── math/
    │   ├── GenerateProblemUseCase.kt       # Smart generation, adaptive difficulty
    │   ├── CheckAnswerUseCase.kt
    │   └── SaveMathSessionUseCase.kt
    ├── points/
    │   ├── AwardPointsUseCase.kt
    │   ├── GetTotalPointsUseCase.kt
    │   └── CheckDailyChallengeUseCase.kt
    ├── avatar/
    │   ├── GetAvatarConfigUseCase.kt
    │   ├── UpdateAvatarUseCase.kt
    │   └── PurchaseItemUseCase.kt
    └── backup/
        ├── CreateBackupUseCase.kt
        ├── RestoreBackupUseCase.kt
        └── ExportProgressReportUseCase.kt
```

**Key implementation: `GenerateProblemUseCase.kt`**

```kotlin
class GenerateProblemUseCase @Inject constructor() {

    /**
     * Generate a math problem based on selected operators and number range.
     * Rules:
     * - No division with remainders (unless configured)
     * - No trivial problems (×0, ×1, +0) at medium+ difficulty
     * - Power limited to sensible ranges (base 2-5, exponent 1-3)
     * - Subtraction results are non-negative
     * - Adaptive: increase difficulty based on current streak
     */
    operator fun invoke(
        operators: Set<Operator>,
        range: IntRange,
        difficulty: Difficulty,
        currentStreak: Int
    ): MathProblem {
        // Implementation:
        // 1. Pick random operator from selected set
        // 2. Generate operands within range
        // 3. Validate constraints (no remainder for div, no negative for sub)
        // 4. If adaptive, expand range by streak/5 steps
        // 5. Return MathProblem(operandA, operandB, operator, correctAnswer)
    }
}
```

### 4.3 core-data

Purpose: Room database, DAOs, repository implementations.

```
core/core-data/src/main/kotlin/com/studybuddy/core/data/
├── db/
│   ├── StudyBuddyDatabase.kt      # @Database with all entities
│   ├── Converters.kt              # TypeConverters for enums, sets, dates
│   ├── entity/
│   │   ├── ProfileEntity.kt
│   │   ├── DicteeListEntity.kt
│   │   ├── DicteeWordEntity.kt
│   │   ├── MathSessionEntity.kt
│   │   ├── PointEventEntity.kt
│   │   ├── AvatarConfigEntity.kt
│   │   ├── OwnedRewardEntity.kt
│   │   └── VoicePackEntity.kt
│   ├── dao/
│   │   ├── ProfileDao.kt
│   │   ├── DicteeDao.kt
│   │   ├── MathDao.kt
│   │   ├── PointsDao.kt
│   │   ├── AvatarDao.kt
│   │   ├── RewardsDao.kt
│   │   └── VoicePackDao.kt
│   └── migration/
│       └── Migrations.kt           # Room migration strategies
├── repository/
│   ├── LocalProfileRepository.kt
│   ├── LocalDicteeRepository.kt
│   ├── LocalMathRepository.kt
│   ├── LocalPointsRepository.kt
│   ├── LocalAvatarRepository.kt
│   ├── LocalRewardsRepository.kt
│   ├── LocalBackupRepository.kt
│   ├── DataStoreSettingsRepository.kt
│   └── LocalVoicePackRepository.kt
├── mapper/
│   ├── ProfileMapper.kt            # Entity <-> Domain model
│   ├── DicteeMapper.kt
│   └── MathMapper.kt
├── datasource/
│   └── RemoteDataSource.kt         # Interface stub for future cloud sync
├── backup/
│   ├── BackupManager.kt            # JSON serialization of full DB
│   ├── PdfReportGenerator.kt       # Android Canvas -> PDF
│   └── CsvExporter.kt             # Word lists to CSV
└── di/
    ├── DatabaseModule.kt            # @Provides Room database and DAOs
    └── RepositoryModule.kt          # @Binds repository implementations
```

**Key implementation: `StudyBuddyDatabase.kt`**

```kotlin
@Database(
    entities = [
        ProfileEntity::class,
        DicteeListEntity::class,
        DicteeWordEntity::class,
        MathSessionEntity::class,
        PointEventEntity::class,
        AvatarConfigEntity::class,
        OwnedRewardEntity::class,
        VoicePackEntity::class,
    ],
    version = 1,
    exportSchema = true  // Required for migration testing
)
@TypeConverters(Converters::class)
abstract class StudyBuddyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun dicteeDao(): DicteeDao
    abstract fun mathDao(): MathDao
    abstract fun pointsDao(): PointsDao
    abstract fun avatarDao(): AvatarDao
    abstract fun rewardsDao(): RewardsDao
    abstract fun voicePackDao(): VoicePackDao
}
```

**Key implementation: `DatabaseModule.kt`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyBuddyDatabase =
        Room.databaseBuilder(
            context,
            StudyBuddyDatabase::class.java,
            "studybuddy.db"
        )
        .fallbackToDestructiveMigration()  // Only for dev; use proper migrations in prod
        .build()

    @Provides fun provideProfileDao(db: StudyBuddyDatabase) = db.profileDao()
    @Provides fun provideDicteeDao(db: StudyBuddyDatabase) = db.dicteeDao()
    @Provides fun provideMathDao(db: StudyBuddyDatabase) = db.mathDao()
    @Provides fun providePointsDao(db: StudyBuddyDatabase) = db.pointsDao()
    @Provides fun provideAvatarDao(db: StudyBuddyDatabase) = db.avatarDao()
    @Provides fun provideRewardsDao(db: StudyBuddyDatabase) = db.rewardsDao()
    @Provides fun provideVoicePackDao(db: StudyBuddyDatabase) = db.voicePackDao()
}
```

**Key implementation: `RepositoryModule.kt`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindProfileRepo(impl: LocalProfileRepository): ProfileRepository
    @Binds abstract fun bindDicteeRepo(impl: LocalDicteeRepository): DicteeRepository
    @Binds abstract fun bindMathRepo(impl: LocalMathRepository): MathRepository
    @Binds abstract fun bindPointsRepo(impl: LocalPointsRepository): PointsRepository
    @Binds abstract fun bindAvatarRepo(impl: LocalAvatarRepository): AvatarRepository
    @Binds abstract fun bindRewardsRepo(impl: LocalRewardsRepository): RewardsRepository
    @Binds abstract fun bindBackupRepo(impl: LocalBackupRepository): BackupRepository
    @Binds abstract fun bindSettingsRepo(impl: DataStoreSettingsRepository): SettingsRepository
    @Binds abstract fun bindVoicePackRepo(impl: LocalVoicePackRepository): VoicePackRepository

    // CLOUD MIGRATION: When ready, create CloudXxxRepository implementations and
    // swap the @Binds here. No other code changes needed.
    // Example:
    // @Binds abstract fun bindDicteeRepo(impl: CloudDicteeRepository): DicteeRepository
}
```

### 4.4 core-ui

Purpose: Design system, theme, shared composables.

```
core/core-ui/src/main/kotlin/com/studybuddy/core/ui/
├── theme/
│   ├── Theme.kt                    # StudyBuddyTheme composable with dynamic color
│   ├── Color.kt                    # Color tokens per theme (Sunset, Ocean, etc.)
│   ├── Type.kt                     # Nunito + DM Sans typography
│   ├── Shape.kt                    # Rounded corner system (16dp cards, etc.)
│   └── ThemeConfig.kt              # Data class for theme selection from rewards
├── components/
│   ├── StudyBuddyCard.kt           # Reusable card with consistent styling
│   ├── StudyBuddyButton.kt         # Primary and secondary button variants
│   ├── PointsBadge.kt              # ⭐ points display with animation
│   ├── StreakIndicator.kt          # 🔥 streak with flame animation
│   ├── ProgressBar.kt             # Animated progress with color transitions
│   ├── AvatarComposite.kt         # Renders full avatar with accessories
│   ├── EmptyState.kt              # Friendly empty state with illustration
│   ├── LoadingState.kt            # Bouncing dots loading indicator
│   └── ErrorState.kt              # Error with retry button
├── animation/
│   ├── CelebrationOverlay.kt      # Confetti / fireworks / etc (Lottie)
│   ├── CorrectAnswerAnimation.kt  # Green glow + scale + points fly-up
│   ├── IncorrectAnimation.kt      # Gentle shake + encouragement
│   ├── StreakFireAnimation.kt     # Animated flame that grows with streak
│   └── PointsFlyUp.kt            # Points text animates upward and fades
├── navigation/
│   └── StudyBuddyNavHost.kt       # Top-level navigation with animations
└── modifier/
    ├── BounceClick.kt              # Modifier.bounceClick() for tactile feedback
    └── ShakeAnimation.kt           # Modifier.shake() for wrong answers
```

**Key implementation: `Theme.kt`**

```kotlin
@Composable
fun StudyBuddyTheme(
    themeConfig: ThemeConfig = ThemeConfig.Sunset,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeConfig) {
        ThemeConfig.Sunset -> SunsetColorScheme
        ThemeConfig.Ocean -> OceanColorScheme
        ThemeConfig.Forest -> ForestColorScheme
        ThemeConfig.Galaxy -> GalaxyColorScheme
        ThemeConfig.Candy -> CandyColorScheme
        ThemeConfig.Arctic -> ArcticColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudyBuddyTypography,
        shapes = StudyBuddyShapes,
        content = content
    )
}
```

---

## 5. Feature: Home

**Module:** `feature/feature-home`
**Dependencies:** `core-domain`, `core-ui`, `shared-points`

### MVI State

```kotlin
data class HomeState(
    val profileName: String = "",
    val avatarConfig: AvatarConfig = AvatarConfig.default(),
    val totalPoints: Long = 0,
    val streakDays: Int = 0,
    val dailyChallengeProgress: Int = 0,     // 0..dailyChallengeGoal
    val dailyChallengeGoal: Int = 5,
    val recentActivities: List<ActivityItem> = emptyList(),
    val greeting: String = "",               // Time-of-day + locale-aware
    val isLoading: Boolean = true,
)

sealed interface HomeIntent {
    data object LoadHome : HomeIntent
    data class NavigateToMode(val mode: StudyMode) : HomeIntent
    data object NavigateToAvatar : HomeIntent
}

sealed interface HomeEffect {
    data class NavigateTo(val route: String) : HomeEffect
}
```

### Implementation Checklist

- [ ] HomeViewModel: Load profile, points, streak, recent activity from repositories
- [ ] Greeting logic: "Bonjour" / "Good morning" / "Guten Morgen" based on locale + time
- [ ] Streak banner: Calculate consecutive days with at least 1 session
- [ ] Daily challenge progress: Count today's completed activities
- [ ] Mode cards: Dictée, Speed Math active; Poems, More locked with 🔒
- [ ] Recent activity: Last 5 sessions with points earned
- [ ] Avatar tap → navigate to Avatar Closet
- [ ] Points badge animates when value changes

---

## 6. Feature: Dictée

**Module:** `feature/feature-dictee`
**Dependencies:** `core-domain`, `core-ui`, `shared-points`, `shared-tts`, `shared-ink`

### Screens

1. **DicteeListScreen** — all word lists with mastery %
2. **DicteeWordEntryScreen** — add/remove words, preview with TTS
3. **DicteePracticeScreen** — keyboard or handwriting input
4. **DicteeFeedbackScreen** — correct/incorrect result

### MVI State (Practice)

```kotlin
data class DicteePracticeState(
    val listTitle: String = "",
    val words: List<DicteeWord> = emptyList(),
    val currentIndex: Int = 0,
    val userInput: String = "",
    val inputMode: InputMode = InputMode.KEYBOARD,
    val feedback: Feedback? = null,
    val sessionScore: Int = 0,
    val streak: Int = 0,
    val isPlaying: Boolean = false,         // TTS currently speaking
    val hintVisible: Boolean = false,
    val recognizedText: String? = null,     // ML Kit handwriting result
)
```

### Implementation Checklist

- [ ] Word list CRUD with Room
- [ ] TTS integration: play word in correct locale (FR/EN/DE) using `shared-tts`
- [ ] Keyboard practice mode with EditText
- [ ] Handwriting canvas using `shared-ink` (ML Kit Digital Ink)
- [ ] Spelling comparison: `CheckSpellingUseCase` with configurable accent strictness
- [ ] Hint system: first letter, word length, play word slowly
- [ ] Scoring: typed = 10pts, handwritten = 15pts, perfect list = 50pts bonus
- [ ] Feedback animations: confetti for correct, gentle shake for incorrect
- [ ] Session summary with per-word results
- [ ] Update mastery % per word after each attempt
- [ ] Random word order weighted by inverse mastery (harder words appear more)

### TTS Module (`shared/shared-tts`)

```kotlin
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _state = MutableStateFlow<TtsState>(TtsState.Initializing)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    fun initialize() { /* Init TTS engine, check available voices */ }
    fun speak(text: String, locale: Locale, speed: Float = 1.0f) { /* ... */ }
    fun stop() { /* ... */ }
    fun getInstalledVoices(): List<VoicePack> { /* ... */ }
    fun downloadVoice(locale: Locale): Flow<DownloadProgress> { /* ... */ }
    fun isLocaleAvailable(locale: Locale): Boolean { /* ... */ }
    fun release() { /* ... */ }
}
```

### Ink Module (`shared/shared-ink`)

```kotlin
@Singleton
class InkRecognitionManager @Inject constructor() {
    private var recognizer: DigitalInkRecognizer? = null

    fun initialize(languageTag: String) {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
        // Download model if needed, create recognizer
    }

    suspend fun recognize(ink: Ink): Result<String> {
        // Returns recognized text from stylus strokes
    }

    fun isModelDownloaded(languageTag: String): Boolean { /* ... */ }
    fun downloadModel(languageTag: String): Flow<DownloadProgress> { /* ... */ }
}
```

---

## 7. Feature: Speed Math

**Module:** `feature/feature-math`
**Dependencies:** `core-domain`, `core-ui`, `shared-points`

### Screens

1. **MathSetupScreen** — operator selection, range, timer, count
2. **MathPlayScreen** — problem display, numpad, timer, streak
3. **MathResultsScreen** — score, accuracy, streak, points

### MVI State (Play)

```kotlin
data class MathPlayState(
    val currentProblem: MathProblem? = null,
    val userAnswer: String = "",
    val feedback: Feedback? = null,
    val problemsCompleted: Int = 0,
    val totalProblems: Int = 20,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val sessionScore: Int = 0,
    val timeRemainingMs: Long = 15_000,
    val timerTotal: Long = 15_000,
    val isPaused: Boolean = false,
    val isComplete: Boolean = false,
    val responseTimesMs: List<Long> = emptyList(),
)

sealed interface MathPlayIntent {
    data class DigitEntered(val digit: Int) : MathPlayIntent
    data object Backspace : MathPlayIntent
    data object Submit : MathPlayIntent
    data object Pause : MathPlayIntent
    data object Resume : MathPlayIntent
}
```

### Implementation Checklist

- [ ] Setup screen: multi-select operators, range slider (1–17), timer options, problem count
- [ ] Problem generation: `GenerateProblemUseCase` with all constraints
- [ ] Countdown timer with `LaunchedEffect` + `delay`
- [ ] Numpad: big touch targets (min 48dp), backspace, submit
- [ ] Streak tracking with multiplier display
- [ ] Auto-submit on timer expiry (mark as incorrect)
- [ ] Feedback: green pop for correct, red shake for incorrect, show correct answer
- [ ] Streak milestones (5, 10, 20): special celebration animation
- [ ] Results screen: score, accuracy %, best streak, avg response time
- [ ] Badge check: unlock "Speed Demon" if avg < 3s, "Streak Master" if streak 10+
- [ ] Save session to Room via `SaveMathSessionUseCase`
- [ ] Adaptive difficulty: expand range by `streak / 5` within session
- [ ] Pause overlay: resume or quit

---

## 8. Feature: Avatar System

**Module:** `feature/feature-avatar`
**Dependencies:** `core-domain`, `core-ui`

### Screens

1. **AvatarClosetScreen** — character selection, accessory tabs, equip/preview

### Data Model

```kotlin
data class AvatarConfig(
    val bodyId: String,          // "fox", "unicorn", etc.
    val hatId: String,           // "none", "crown", etc.
    val faceId: String,
    val outfitId: String,
    val petId: String,
    val equippedTitle: String?,  // "Rising Star", etc.
)

data class RewardItem(
    val id: String,
    val category: RewardCategory,    // HAT, FACE, OUTFIT, PET, THEME, EFFECT, SOUND, TITLE
    val name: String,
    val icon: String,                // Emoji or drawable res
    val cost: Int,                   // Star cost, 0 = free/starter
    val description: String?,
)
```

### Implementation Checklist

- [ ] Avatar composite composable: layers body + hat + face + outfit + pet
- [ ] Character selection grid (8 characters)
- [ ] Accessory tabs: Hats, Face, Outfits, Pets
- [ ] Each item shows: owned ✓, locked with cost, or equipped highlight
- [ ] Tap owned item → equip immediately, update AvatarConfig in Room
- [ ] Tap locked item → show purchase confirmation dialog with star cost
- [ ] Purchase flow: deduct stars via PointsRepository, add to OwnedRewards, equip
- [ ] Avatar preview updates in real-time as selections change
- [ ] Avatar composable reused on: Home header, Settings profile card, Onboarding

---

## 9. Feature: Rewards Shop

**Module:** `feature/feature-rewards`
**Dependencies:** `core-domain`, `core-ui`, `shared-points`

### Tabs

1. **Avatar** — hats, face accessories, outfits, pets (links to avatar closet purchase)
2. **Themes** — app color themes with gradient preview
3. **Effects** — celebration animations + correct answer sounds
4. **Titles** — achievement-based titles, equip under profile name

### Implementation Checklist

- [ ] Tab layout with horizontal pager
- [ ] Points balance badge (top right, always visible)
- [ ] Avatar tab: grid of items grouped by category, owned/locked state
- [ ] Themes tab: gradient preview cards, "Active" badge on current
- [ ] Effects tab: celebration grid + sound list
- [ ] Titles tab: list with requirement description, "Equip" button for unlocked
- [ ] Purchase dialog: confirm, show star deduction, success animation
- [ ] Insufficient stars: show how many more needed, gentle message
- [ ] Theme purchase → persist to DataStore → `StudyBuddyTheme` reads it
- [ ] Effect purchase → persist selection → `CelebrationOverlay` reads it
- [ ] All reward definitions stored in `RewardCatalog.kt` (hardcoded list with costs)
- [ ] Owned items stored in `OwnedRewardEntity` Room table

---

## 10. Feature: Stats & Progress

**Module:** `feature/feature-stats`
**Dependencies:** `core-domain`, `core-ui`

### Implementation Checklist

- [ ] Total stars, streak days, session count summary cards
- [ ] Weekly bar chart: points earned per day (Compose Canvas)
- [ ] Dictée accuracy trend: last 4 weeks percentage
- [ ] Math speed trend: average response time over sessions
- [ ] Badge collection: earned vs locked with progress hints
- [ ] Per-mode detail screens (optional, future)

### Chart Implementation

Use Compose Canvas for the weekly chart (no external library needed):

```kotlin
@Composable
fun WeeklyChart(data: List<DayData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        // Draw bars with rounded tops
        // Highlight today's bar with primary color
        // Animate bar height with animateFloatAsState
    }
}
```

---

## 11. Feature: Settings

**Module:** `feature/feature-settings`
**Dependencies:** `core-domain`, `core-ui`, `shared-tts`

### Storage

Use DataStore Preferences for all settings:

```kotlin
object SettingsKeys {
    val APP_LOCALE = stringPreferencesKey("app_locale")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val DAILY_GOAL = intPreferencesKey("daily_goal")
    val ACCENT_STRICT = booleanPreferencesKey("accent_strict")
    val DEFAULT_TIMER_SECONDS = intPreferencesKey("default_timer_seconds")
    val DEFAULT_INPUT_MODE = stringPreferencesKey("default_input_mode")
    val HINTS_ENABLED = booleanPreferencesKey("hints_enabled")
    val SELECTED_THEME = stringPreferencesKey("selected_theme")
    val SELECTED_CELEBRATION = stringPreferencesKey("selected_celebration")
    val SELECTED_SOUND = stringPreferencesKey("selected_sound")
    val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    val PARENT_PIN = stringPreferencesKey("parent_pin")
    val SCREEN_TIME_LIMIT_MINUTES = intPreferencesKey("screen_time_limit")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
```

### Implementation Checklist

- [ ] Profile card with avatar (tap → Avatar Closet)
- [ ] General section: language, reminders, sound, haptic toggles
- [ ] Voice Packs section: installed/available, download/remove (→ VoicePackManager screen)
- [ ] Learning section: daily goal, accent strictness, timer, input mode, hints
- [ ] Parent Zone 🔒: gated behind PIN entry dialog
- [ ] Parent Zone items: Progress Reports, Screen Time, Backup & Export, Cloud Sync (disabled), Reset
- [ ] Study reminders: schedule via WorkManager `PeriodicWorkRequest`
- [ ] Screen time limits: track session time, show "time's up" overlay

---

## 12. Feature: Backup & Export

**Module:** `feature/feature-backup`
**Dependencies:** `core-domain`, `core-data`

### Implementation Checklist

- [ ] Manual backup: serialize entire Room DB to JSON, save to app-specific storage
- [ ] Restore: parse JSON, validate schema version, write to Room
- [ ] Auto-backup: WorkManager periodic task (daily/weekly)
- [ ] Export PDF: Android `PdfDocument` API → rendered progress report with charts
- [ ] Export JSON: raw database dump for migration
- [ ] Export CSV: dictée word lists as comma-separated files
- [ ] Email report: create PDF, open share intent with email apps
- [ ] Storage usage display: query Room DB size + voice pack sizes
- [ ] Backup file naming: `studybuddy_backup_{date}_{profileName}.json`
- [ ] Share backup via Android share sheet (Google Drive, Files, email)
- [ ] Restore confirmation dialog with data loss warning

### Backup JSON Schema

```json
{
  "version": 1,
  "exportedAt": "2026-02-20T10:30:00Z",
  "profile": { "name": "Léa", "locale": "fr", "createdAt": "..." },
  "avatarConfig": { "bodyId": "unicorn", "hatId": "party", ... },
  "ownedRewards": ["crown", "ocean-theme", ...],
  "dicteeLists": [
    {
      "title": "Liste du lundi",
      "language": "fr",
      "words": [
        { "word": "maison", "attempts": 5, "correctCount": 4, ... }
      ]
    }
  ],
  "mathSessions": [ ... ],
  "pointEvents": [ ... ],
  "settings": { ... }
}
```

---

## 13. Feature: Offline TTS

**Module:** `shared/shared-tts`

### Voice Pack Download Flow

```
1. Check Android TTS engine voices available on device
2. For each supported locale (FR, EN, DE):
   a. Check if high-quality voice is installed
   b. If not, show download prompt
   c. Use TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA intent
   d. Or download via TTS engine's built-in mechanism
3. Track download status in VoicePackEntity (Room)
4. Settings screen shows installed/available with download/remove
5. Dictée practice checks voice availability before starting
6. Fallback: use default system TTS if neural voice unavailable
```

### Implementation Checklist

- [ ] TtsManager singleton with init, speak, stop, release
- [ ] Voice pack status tracking in Room (downloading, installed, failed)
- [ ] Download progress reporting via Flow
- [ ] Onboarding step: download voices with progress UI
- [ ] Settings → Voice Pack Manager: test, update, remove installed voices
- [ ] Available voices list with download buttons
- [ ] Storage size display per voice pack
- [ ] Locale-aware voice selection: pick best voice per language
- [ ] Speed control: normal (1.0), slow (0.7) for practice
- [ ] Error handling: fallback to system TTS, user-friendly error messages
- [ ] Check network for downloads, show offline message if unavailable

---

## 14. Feature: Onboarding

**Module:** `feature/feature-onboarding`
**Dependencies:** `core-domain`, `core-ui`, `shared-tts`

### Flow (3 steps)

```
Step 1: Welcome
  - Enter name
  - Select app language (FR/EN/DE)
  - [Next →]

Step 2: Choose Your Buddy
  - Pick character (8 options)
  - Select starter hat (from owned items)
  - Select starter face accessory (from owned items)
  - Preview avatar live
  - Hint: "Earn ⭐ stars to unlock more!"
  - [Next →]

Step 3: Download Voices
  - Show FR/EN/DE voice packs
  - Auto-start downloads
  - Progress bars per language
  - "Skip for now" option
  - [Let's Go! →]

→ Save profile + avatar to Room
→ Set ONBOARDING_COMPLETE = true
→ Navigate to Home
```

### Implementation Checklist

- [ ] Horizontal pager with 3 steps
- [ ] Step indicator dots
- [ ] Name input with validation (non-empty)
- [ ] Language selector with flag icons
- [ ] Character grid with selection highlight
- [ ] Starter accessories (only show owned/free items)
- [ ] Avatar live preview as selections change
- [ ] Voice download with progress tracking
- [ ] Skip downloads option
- [ ] Save all data on completion
- [ ] Gate Home screen behind `ONBOARDING_COMPLETE` check

---

## 15. Testing Strategy

### 15.1 Unit Tests (every module)

| Layer | What to Test | Tool |
|---|---|---|
| Use Cases | Business logic, edge cases | JUnit 5 + MockK |
| ViewModels | State transitions per intent | JUnit 5 + Turbine + MockK |
| Repositories | Data mapping, query logic | JUnit 5 + Room in-memory DB |
| Mappers | Entity ↔ Domain correctness | JUnit 5 (no mocks) |
| Utilities | String comparison, point calc | JUnit 5 parameterized |

### 15.2 Key Test Cases

**Dictée Spelling:**
```kotlin
@ParameterizedTest
@CsvSource(
    "maison, maison, true, true",        // exact match
    "Maison, maison, true, true",        // case insensitive
    "château, chateau, false, true",     // accent lenient
    "château, chateau, true, false",     // accent strict fails
    "bibliothèque, bibliotheque, false, true",
    "château, chateu, false, false",     // wrong letter, both fail
)
fun `spelling comparison`(input: String, target: String, strict: Boolean, expected: Boolean) {
    assertEquals(expected, input.matchesWord(target, strict))
}
```

**Math Problem Generation:**
```kotlin
@RepeatedTest(100)
fun `division problems have no remainder`() {
    val problem = generateProblem(setOf(Operator.DIVIDE), 1..12, Difficulty.MEDIUM, 0)
    assertEquals(0, problem.operandA % problem.operandB)
}

@RepeatedTest(100)
fun `subtraction results are non-negative`() {
    val problem = generateProblem(setOf(Operator.MINUS), 1..17, Difficulty.MEDIUM, 0)
    assertTrue(problem.correctAnswer >= 0)
}

@Test
fun `power problems stay within sensible range`() {
    repeat(100) {
        val problem = generateProblem(setOf(Operator.POWER), 2..5, Difficulty.MEDIUM, 0)
        assertTrue(problem.operandB in 1..3)
        assertTrue(problem.correctAnswer <= 125)  // 5^3 max
    }
}
```

**Points System:**
```kotlin
@Test
fun `streak multiplier applies correctly`() {
    assertEquals(1.0, PointValues.streakMultiplier(0))
    assertEquals(1.0, PointValues.streakMultiplier(4))
    assertEquals(1.5, PointValues.streakMultiplier(5))
    assertEquals(2.0, PointValues.streakMultiplier(10))
    assertEquals(3.0, PointValues.streakMultiplier(20))
}

@Test
fun `perfect list awards completion bonus`() {
    val points = calculateDicteePoints(totalWords = 8, correctWords = 8, inputMode = KEYBOARD)
    assertEquals(8 * 10 + 50, points)  // 80 + 50 = 130
}
```

**ViewModel Tests (with Turbine):**
```kotlin
@Test
fun `correct answer increments streak and score`() = runTest {
    val viewModel = MathPlayViewModel(/* mocked deps */)

    viewModel.state.test {
        val initial = awaitItem()
        assertEquals(0, initial.streak)

        viewModel.onIntent(MathPlayIntent.DigitEntered(1))
        viewModel.onIntent(MathPlayIntent.DigitEntered(9))
        viewModel.onIntent(MathPlayIntent.Submit)

        val afterSubmit = expectMostRecentItem()
        assertEquals(Feedback.Correct, afterSubmit.feedback)
        assertEquals(1, afterSubmit.streak)
        assertTrue(afterSubmit.sessionScore > 0)
    }
}
```

### 15.3 UI Tests (Compose)

```kotlin
@Test
fun `numpad displays all digits and controls`() {
    composeTestRule.setContent {
        MathNumpad(onDigit = {}, onBackspace = {}, onSubmit = {})
    }

    (0..9).forEach { digit ->
        composeTestRule.onNodeWithText("$digit").assertExists().assertIsDisplayed()
    }
    composeTestRule.onNodeWithText("⌫").assertExists()
    composeTestRule.onNodeWithText("✓").assertExists()
}
```

### 15.4 Database Tests

```kotlin
@Test
fun `insert and retrieve dictee words`() = runTest {
    val dao = database.dicteeDao()
    val listId = dao.insertList(DicteeListEntity(title = "Test", language = "fr"))
    dao.insertWord(DicteeWordEntity(listId = listId, word = "maison"))

    val words = dao.getWordsForList(listId).first()
    assertEquals(1, words.size)
    assertEquals("maison", words[0].word)
}
```

### 15.5 Coverage Targets

| Module | Minimum Coverage |
|---|---|
| `core-domain` (use cases) | 90% |
| `core-data` (repositories, mappers) | 80% |
| `core-common` (utilities) | 95% |
| `shared-points` | 90% |
| Feature ViewModels | 80% |
| UI composables | 60% (focused on interactions) |

---

## 16. Release & Signing

### 16.1 Generate Keystore

```bash
keytool -genkey -v \
  -keystore studybuddy-release.keystore \
  -alias studybuddy \
  -keyalg RSA -keysize 2048 \
  -validity 10000

# Base64 encode for GitHub secret
base64 -i studybuddy-release.keystore -o keystore-base64.txt
# Copy contents to KEYSTORE_BASE64 GitHub secret
```

### 16.2 App `build.gradle.kts` Signing Config

```kotlin
android {
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 16.3 Release Process

```bash
# 1. Create release branch
git checkout develop
git pull
git checkout -b release/1.0.0

# 2. Update version in app/build.gradle.kts
# versionCode = 1
# versionName = "1.0.0"

# 3. PR to main, merge after approval

# 4. Tag the release
git checkout main
git pull
git tag -a v1.0.0 -m "Release 1.0.0: Initial launch with Dictée and Speed Math"
git push origin v1.0.0
# → Triggers release.yml pipeline automatically
```

---

## 17. Cloud Migration Hooks

The codebase is designed for zero-UI-change cloud migration. Here are the touchpoints:

### 17.1 What to Implement When Ready

```kotlin
// 1. Add network client
// build.gradle.kts → add Retrofit/Ktor dependency

// 2. Create RemoteDataSource implementation
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteDataSource { ... }

// 3. Create syncing repository
class CloudDicteeRepository @Inject constructor(
    private val local: DicteeDao,
    private val remote: FirestoreDataSource,
    private val syncManager: SyncManager
) : DicteeRepository {
    override suspend fun sync() {
        // Pull remote changes
        // Push local changes
        // Resolve conflicts (last-write-wins or custom)
    }
}

// 4. Swap in RepositoryModule.kt
@Binds abstract fun bindDicteeRepo(impl: CloudDicteeRepository): DicteeRepository
// That's it. No UI changes.

// 5. Add sync trigger
// WorkManager periodic sync + on-demand sync button in Settings
```

### 17.2 Sync-Ready Patterns Already in Place

| Pattern | Location | Purpose |
|---|---|---|
| `sync()` method on all repositories | `core-domain/repository/` | No-op locally, real sync later |
| `updatedAt` timestamp on all entities | `core-data/entity/` | Conflict resolution |
| UUID primary keys | All entities | Avoid ID collisions across devices |
| Repository interface abstraction | `core-domain/repository/` | Swap implementations via DI |
| Hilt module bindings | `core-data/di/RepositoryModule.kt` | Single point to swap local→cloud |

---

## 18. Appendix: Coding Standards

### 18.1 Kotlin Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Max line length: 120 characters
- Use `data class` for all state objects
- Use `sealed interface` for intents and effects
- Prefer `when` over `if-else` chains
- Use named arguments for functions with 3+ parameters

### 18.2 Compose Rules

- Stateless composables preferred (state hoisting)
- Preview every screen with `@Preview` annotation and sample data
- Use `Modifier` as first optional parameter
- Extract repeated styles to theme
- Use `remember` and `derivedStateOf` to avoid recomposition

### 18.3 MVI Pattern

Every feature ViewModel follows this structure:

```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val useCase: XxxUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(XxxState())
    val state: StateFlow<XxxState> = _state.asStateFlow()

    private val _effect = Channel<XxxEffect>()
    val effect: Flow<XxxEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: XxxIntent) {
        when (intent) {
            is XxxIntent.Action -> handleAction(intent)
        }
    }

    private fun handleAction(intent: XxxIntent.Action) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // ... business logic
            _state.update { it.copy(isLoading = false, data = result) }
        }
    }
}
```

### 18.4 Git Commit Convention

```
type(scope): description

Types: feat, fix, chore, docs, test, refactor, style, perf
Scopes: app, core, dictee, math, avatar, rewards, stats, settings, backup, tts, onboarding, ci

Examples:
feat(dictee): add handwriting input mode with ML Kit recognition
fix(math): prevent division by zero in problem generation
test(points): add parameterized tests for streak multiplier
chore(ci): add code coverage pipeline with Codecov
```

### 18.5 PR Template

Create `.github/pull_request_template.md`:

```markdown
## Description
Brief description of changes.

## Type
- [ ] Feature
- [ ] Bug fix
- [ ] Refactor
- [ ] Chore / CI

## Module(s) Changed
- [ ] core
- [ ] feature-dictee
- [ ] feature-math
- [ ] feature-avatar
- [ ] feature-rewards
- [ ] feature-stats
- [ ] feature-settings
- [ ] feature-backup
- [ ] shared-tts
- [ ] shared-ink
- [ ] shared-points

## Testing
- [ ] Unit tests added/updated
- [ ] UI tests added/updated
- [ ] Manual testing performed

## Screenshots / Videos
(Attach if UI changes)
```

---

## Build Order (Recommended)

Execute phases in this order. Each phase should be a PR to `develop`.

```
Phase 1: Foundation
  ├── PR 1: Project scaffold + CI pipelines + build-logic
  ├── PR 2: core-common (extensions, constants, result type)
  ├── PR 3: core-domain (all models + repository interfaces + use cases)
  ├── PR 4: core-data (Room DB, all entities, all DAOs, repository impls)
  ├── PR 5: core-ui (theme, design system, shared components, animations)
  └── PR 6: shared-points (point calculation, PointsBadge, StreakIndicator)

Phase 2: Dictée
  ├── PR 7: shared-tts (TTS manager, voice pack tracking)
  ├── PR 8: shared-ink (ML Kit ink recognition wrapper)
  ├── PR 9: feature-dictee (list screen, word entry, practice, feedback)
  └── PR 10: feature-dictee tests

Phase 3: Speed Math
  ├── PR 11: feature-math (setup, play, results)
  └── PR 12: feature-math tests

Phase 4: Avatar & Rewards
  ├── PR 13: feature-avatar (closet screen, purchase flow)
  ├── PR 14: feature-rewards (shop with 4 tabs)
  └── PR 15: tests

Phase 5: Infrastructure
  ├── PR 16: feature-settings (all sections, voice pack manager)
  ├── PR 17: feature-backup (backup, restore, export)
  ├── PR 18: feature-stats (progress, charts, badges)
  └── PR 19: feature-onboarding (3-step flow)

Phase 6: Integration & Polish
  ├── PR 20: feature-home (connect all data sources)
  ├── PR 21: app module (navigation, DI wiring, entry point)
  ├── PR 22: animation polish pass
  ├── PR 23: localization (FR, EN, DE strings)
  ├── PR 24: accessibility pass (content descriptions, touch targets)
  └── PR 25: final QA + release pipeline test
```

---

*Document version: 2.0 — February 2026*
*Companion documents: StudyBuddy App Planning Document v1.0, StudyBuddy Prototype v2*
