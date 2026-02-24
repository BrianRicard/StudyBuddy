# StudyBuddy

A fun, animated study aid for kids ages 6-10, built with Kotlin and Jetpack Compose. Features Dictee (spelling practice with TTS and handwriting recognition), Speed Math, Avatar dress-up, and a Rewards Shop.

## Prerequisites

### System Requirements

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| OS | Linux x86_64 (Ubuntu 22.04+) | Ubuntu 24.04 LTS |
| RAM | 8 GB | 16 GB |
| Disk | 20 GB free | 40 GB free |
| CPU | x86_64 with virtualization | AMD EPYC / Intel Xeon with nested virt |

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | **17** | AGP 8.7.3 requires JDK 17 (JDK 21 causes BouncyCastle signing errors) |
| Android SDK | API 35 (compile), API 26+ (min) | Build target |
| Android Build Tools | 35.0.0 | APK packaging |
| Android SDK Platform Tools | Latest | `adb` for device/emulator communication |
| Git | 2.x+ | Version control |

> **JDK 17 is required.** JDK 21 is incompatible with AGP 8.7.3 — the `validateSigningDebug` task fails with `NoClassDefFoundError: org/bouncycastle/asn1/edec/EdECObjectIdentifiers`.

### Emulator Requirements (for instrumented tests)

| Requirement | Details |
|-------------|---------|
| KVM | `/dev/kvm` must exist and be accessible |
| KVM group | User must be in the `kvm` group (`sudo gpasswd -a $USER kvm`) |
| System image | `system-images;android-31;google_apis;x86_64` |
| Emulator | Android Emulator (installed via `sdkmanager`) |

For Azure VMs, use a nested-virtualization-capable SKU (Dv3, Dsv3, Ev3, Dv4, Dv5, or similar).

## Setup

### 1. Install JDK 17

```bash
# Ubuntu/Debian
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk unzip wget

# Verify
java -version  # Should show 17.x
```

### 2. Install Android SDK

```bash
# Set SDK location
export ANDROID_HOME=$HOME/android-sdk
mkdir -p $ANDROID_HOME

# Download and install command-line tools
cd /tmp
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -qo commandlinetools-linux-11076708_latest.zip
mkdir -p $ANDROID_HOME/cmdline-tools/latest
cp -r cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/
rm -rf cmdline-tools commandlinetools-linux-11076708_latest.zip

# Accept licenses
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_HOME --licenses

# Install required components
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_HOME \
  "platform-tools" \
  "build-tools;35.0.0" \
  "platforms;android-35" \
  "platforms;android-31"
```

### 3. Set Environment Variables

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$JAVA_HOME/bin:$PATH
```

Then reload: `source ~/.bashrc`

### 4. Create local.properties

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 5. Generate Debug Keystore

A debug keystore is required for building debug APKs:

```bash
mkdir -p ~/.android
keytool -genkey -v -keystore ~/.android/debug.keystore \
  -storepass android -alias androiddebugkey -keypass android \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
```

### 6. Verify Build

```bash
./gradlew assembleDebug
```

## Emulator Setup

### Install emulator and system image

```bash
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_HOME \
  "emulator" \
  "system-images;android-31;google_apis;x86_64"
```

### Enable KVM access

```bash
# Add your user to the kvm group
sudo gpasswd -a $USER kvm

# Apply group change (or log out and back in)
newgrp kvm
```

### Create and launch AVD

```bash
# Create AVD
echo "no" | avdmanager create avd \
  -n studybuddy_test \
  -k "system-images;android-31;google_apis;x86_64" \
  -d pixel_6 --force

# Launch headless (for remote/CI environments)
$ANDROID_HOME/emulator/emulator -avd studybuddy_test \
  -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim &

# Wait for boot
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'
echo "Emulator ready"
```

### Run instrumented tests

```bash
./gradlew connectedDebugAndroidTest
```

## Build Commands

```bash
./gradlew assembleDebug                # Build debug APK
./gradlew testDebugUnitTest            # Run all unit tests
./gradlew lintDebug                    # Android lint
./gradlew detekt                       # Static analysis
./gradlew ktlintCheck                  # Code style
./gradlew koverXmlReportDebug          # Code coverage
./gradlew connectedDebugAndroidTest    # Instrumented tests (needs emulator)
```

## Project Structure

```
app/                          → App shell, nav host, Hilt entry point
build-logic/                  → Convention plugins
core/core-ui/                 → Theme, design system, shared composables
core/core-data/               → Room DB, entities, DAOs, repositories
core/core-domain/             → Domain models, repository interfaces, use cases
core/core-common/             → Extensions, utils, constants
feature/feature-home/         → Home screen
feature/feature-dictee/       → Dictee (spelling with TTS + handwriting)
feature/feature-math/         → Speed Math
feature/feature-avatar/       → Avatar closet
feature/feature-rewards/      → Rewards shop
feature/feature-stats/        → Progress charts, badges
feature/feature-settings/     → Settings, voice pack manager
feature/feature-backup/       → Backup, restore, export
feature/feature-onboarding/   → 3-step onboarding
shared/shared-points/         → Points system
shared/shared-tts/            → TextToSpeech manager
shared/shared-ink/            → ML Kit handwriting recognition
```

## Tech Stack

- **Language:** Kotlin 2.1 + Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture + MVI
- **DI:** Hilt (Dagger)
- **Database:** Room (local SQLite)
- **Async:** Coroutines + Flow
- **Testing:** JUnit 5, Turbine, MockK
- **CI/CD:** GitHub Actions

See [CLAUDE.md](CLAUDE.md) for the full technical specification.
