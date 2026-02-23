# Android Emulator Setup Research

> Researched 2026-02-23 on Azure VM (Standard_D4s_v3 equivalent)

## Current VM Capabilities

| Resource | Status | Details |
|----------|--------|---------|
| CPU | AMD EPYC 7763 | 4 vCPUs (2 cores x 2 threads), Hyper-V hosted |
| KVM | **NOT available** | `/dev/kvm` does not exist — nested virtualization not enabled |
| RAM | 15 GB total | ~9 GB available (sufficient for emulator) |
| Disk | 123 GB total | ~112 GB free (sufficient) |

## Existing Android SDK Components

SDK location: `/home/azureuser/StudyBuddy` (configured in `local.properties`)

| Component | Installed? | Version |
|-----------|-----------|---------|
| platform-tools (adb) | Yes | 34.0.0, 35.0.0 |
| build-tools | Yes | 34.0.0, 35.0.0 |
| cmdline-tools (sdkmanager) | **No** | — |
| emulator | **No** | — |
| system-images | **No** | — |
| AVD configs | **No** | ~/.android/avd/ does not exist |

Environment variables `ANDROID_HOME` and `ANDROID_SDK_ROOT` are **not set**.

## Project Requirements

- minSdk: **26**, targetSdk: **35**, compileSdk: **35**
- Existing instrumented tests in `core/core-data/src/androidTest/`
- CI already uses `reactivecircus/android-emulator-runner@v2` with API 31, x86_64

## Options

### Option A: Enable KVM on the Azure VM (Recommended)

**Prerequisites:** Resize the VM to a size that supports nested virtualization (Dv3, Dsv3, Ev3, Esv3, or newer v4/v5 series with nested virt).

**Steps:**

1. **Resize the VM** in Azure Portal to a nested-virt-capable SKU
2. **Verify KVM is available:**
   ```bash
   ls /dev/kvm
   # Should show /dev/kvm
   ```
3. **Install cmdline-tools:**
   ```bash
   export ANDROID_HOME=/home/azureuser/StudyBuddy
   export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

   # Download cmdline-tools
   cd /tmp
   wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
   unzip commandlinetools-linux-11076708_latest.zip
   mkdir -p $ANDROID_HOME/cmdline-tools/latest
   mv cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/
   ```
4. **Install emulator + system image:**
   ```bash
   sdkmanager --sdk_root=$ANDROID_HOME "emulator" "system-images;android-31;google_apis;x86_64"
   ```
5. **Create AVD:**
   ```bash
   avdmanager create avd -n studybuddy_test -k "system-images;android-31;google_apis;x86_64" -d pixel_6
   ```
6. **Launch emulator:**
   ```bash
   $ANDROID_HOME/emulator/emulator -avd studybuddy_test -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim &
   ```
7. **Wait for boot, then run tests:**
   ```bash
   adb wait-for-device
   adb shell getprop sys.boot_completed  # Wait until returns "1"
   ./gradlew connectedDebugAndroidTest
   ```

**Performance:** With KVM, emulator runs at near-native speed. Expect ~2-5 min for full instrumented test suite.

### Option B: Software-Only Emulation (No KVM)

If the VM cannot be resized, the emulator can still run but **very slowly** using QEMU software emulation.

**Additional steps** (same as Option A but):
- Use ARM system image instead of x86_64 (avoids translation overhead):
  ```bash
  sdkmanager "system-images;android-31;google_apis;arm64-v8a"
  ```
- Launch with extra patience flags:
  ```bash
  emulator -avd studybuddy_test -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -no-snapshot -memory 2048
  ```
- **Boot time:** 5-15 minutes
- **Test execution:** 5-10x slower than KVM

### Option C: Keep Using CI Only

The existing CI pipeline already handles instrumented tests via GitHub Actions (which have KVM). This is the zero-setup option.

- **Pros:** No local setup needed, KVM available on GH runners
- **Cons:** Can't interactively test on emulator, slow feedback loop (~10 min per push)

## Recommended Path

1. **Resize VM** to enable nested virtualization (KVM)
2. **Follow Option A** steps above
3. Use API 31 (same as CI) with `google_apis;x86_64` image
4. Keep the emulator headless (`-no-window`) since this is a remote VM
5. Use `adb` + `scrcpy` or screenshot commands for visual verification if needed

## Quick-Start Script (Post KVM Enable)

Save as `scripts/setup-emulator.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME=/home/azureuser/StudyBuddy
export ANDROID_HOME
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH

API_LEVEL=31
AVD_NAME=studybuddy_test
IMAGE="system-images;android-${API_LEVEL};google_apis;x86_64"

# Step 1: Install cmdline-tools if missing
if [ ! -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    echo "Installing cmdline-tools..."
    cd /tmp
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    unzip -qo commandlinetools-linux-11076708_latest.zip
    mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
    cp -r cmdline-tools/* "$ANDROID_HOME/cmdline-tools/latest/"
    rm -rf cmdline-tools commandlinetools-linux-11076708_latest.zip
fi

# Step 2: Accept licenses
yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Step 3: Install emulator + system image
sdkmanager "emulator" "$IMAGE"

# Step 4: Create AVD
echo "no" | avdmanager create avd -n "$AVD_NAME" -k "$IMAGE" -d pixel_6 --force

# Step 5: Launch emulator
echo "Starting emulator..."
emulator -avd "$AVD_NAME" -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim &
EMULATOR_PID=$!

# Step 6: Wait for boot
echo "Waiting for emulator to boot..."
adb wait-for-device
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null)" != "1" ]; do
    sleep 2
done
echo "Emulator ready! (PID: $EMULATOR_PID)"
echo "Run tests with: ./gradlew connectedDebugAndroidTest"
```
