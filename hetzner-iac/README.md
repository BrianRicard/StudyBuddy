# StudyBuddy — Hetzner VM Infrastructure

OpenTofu (Terraform-compatible) IaC to provision a Hetzner Cloud VM pre-configured for Android CI, builds, emulator testing, and on-device model work (whisper.cpp).

## What Gets Provisioned

| Resource | Details |
|---|---|
| **Server** | `cpx31` — 4 AMD vCPU, 8 GB RAM, 160 GB NVMe, Ubuntu 24.04 |
| **Location** | `nbg1` (Nuremberg, Germany) |
| **Firewall** | SSH (22) + dev HTTP (8080) inbound; all outbound allowed |

The cloud-init script installs: JDK 17, Android SDK 34, KVM/emulator, whisper.cpp (AVX2), Node.js 20 + Claude Code, Gradle, and UFW.

## Prerequisites

- **OpenTofu >= 1.6** — install via `brew install opentofu` or see [opentofu.org/docs/intro/install](https://opentofu.org/docs/intro/install/)
- **Hetzner Cloud API token** — get one from [Hetzner Console](https://console.hetzner.cloud/) → your project → Security → API Tokens → Generate API Token (Read & Write)
- **SSH key pair** — defaults to `~/.ssh/id_rsa.pub`

## Deploy

```bash
cd hetzner-iac

# 1. Create your variables file
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars and paste your hcloud_token
# (or export TF_VAR_hcloud_token="your-token-here")

# 2. Initialize providers
tofu init

# 3. Preview changes
tofu plan

# 4. Apply
tofu apply
```

After `apply` completes, OpenTofu prints the server IP and an SSH command.

> **Note:** Cloud-init takes 5–10 minutes to finish after the server is created. You can SSH in immediately, but some tools may still be installing. Monitor progress with:
> ```bash
> ssh root@<ip> "tail -f /var/log/cloud-init-output.log"
> ```

## Connect

```bash
# Use the output directly
tofu output ssh_command

# Or manually
ssh root@<SERVER_IP>
```

## Verify the Setup

### KVM

```bash
ssh root@<ip> "kvm-ok"
# Expected: INFO: /dev/kvm exists — KVM acceleration can be used
```

### Android SDK

```bash
ssh root@<ip> "adb --version"
ssh root@<ip> "/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --list_installed"
```

### whisper.cpp

```bash
ssh root@<ip> "/opt/whisper.cpp/main -m /opt/whisper.cpp/models/ggml-base.bin --help"
```

### Cloud-init summary log

```bash
ssh root@<ip> "cat /var/log/cloud-init-studybuddy.log"
```

## Create an Android AVD

After cloud-init finishes:

```bash
ssh root@<ip>

# Source the environment (or reconnect for a fresh shell)
source /etc/environment

# Create AVD
avdmanager create avd \
  -n studybuddy_avd \
  -k "system-images;android-34;google_apis;x86_64" \
  --device "pixel_6"

# Launch headless emulator
emulator -avd studybuddy_avd -no-audio -no-window &

# Wait for boot
adb wait-for-device
adb shell getprop sys.boot_completed  # returns "1" when ready
```

## Destroy

```bash
tofu destroy
```

This terminates the server and removes the firewall and SSH key from Hetzner. Local state files remain until you delete them.

## File Structure

```
hetzner-iac/
├── main.tf                  # Provider, server, firewall, SSH key
├── variables.tf             # Input variables (token, SSH key path, location)
├── outputs.tf               # server_ip, ssh_command
├── terraform.tfvars.example # Sample variables file (no secrets)
├── cloud-init.yaml          # Server bootstrap script
├── .gitignore               # Excludes tfstate, .terraform/, terraform.tfvars
└── README.md                # This file
```

## Cost

The `cpx31` server costs approximately **€15.90/month** (billed hourly). Remember to `tofu destroy` when you're done to stop billing.
