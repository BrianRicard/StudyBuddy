# StudyBuddy — Hetzner VM Infrastructure (NixOS)

OpenTofu IaC to provision a Hetzner Cloud VM running NixOS, pre-configured for Android CI, builds, emulator testing, and whisper.cpp.

## How It Works

`tofu apply` is fully unattended — no manual steps after it completes:

1. **Phase 1** — Creates a Hetzner VM with Ubuntu 24.04, runs `provision.sh` via user_data which:
   - Writes all NixOS configuration files to `/root/studybuddy-nixos/`
   - Runs `nixos-infect` to convert Ubuntu → NixOS (VM reboots)

2. **Phase 2** — A `null_resource` provisioner waits for the reboot, then:
   - Copies the generated `hardware-configuration.nix` into the config dir
   - Runs `nixos-rebuild switch --flake .#studybuddy-dev`

After ~15–20 minutes, SSH in as `claude` and start working.

## What Gets Provisioned

| Component | Details |
|---|---|
| **Server** | `ccx13` — 4 AMD vCPU, 8 GB RAM, 160 GB NVMe, NixOS 24.05 |
| **JDK 17** | For Gradle / Android builds |
| **Android SDK** | Platform 34, build-tools 34.0.0, x86_64 emulator, licenses auto-accepted |
| **Node.js 20** | For Claude Code |
| **Claude Code** | Installed globally via npm |
| **Gradle** | System-wide |
| **whisper.cpp** | Built from source with AVX2, base model auto-downloaded |
| **KVM** | Emulator acceleration (CCX dedicated vCPU required) |
| **SSH** | Key-only auth, root login disabled, fail2ban |
| **Firewall** | SSH (22) + dev HTTP (8080) inbound; all outbound |

> **Non-root user:** Claude Code refuses to run as root. All SSH access uses the `claude` user, which has passwordless sudo. Root SSH login is disabled after nixos-rebuild.

## Prerequisites

- **OpenTofu >= 1.6** — `brew install opentofu` or see [opentofu.org](https://opentofu.org/docs/intro/install/)
- **Hetzner Cloud API token** — [Hetzner Console](https://console.hetzner.cloud/) → Project → Security → API Tokens
- **SSH key pair** — defaults to `~/.ssh/id_rsa` and `~/.ssh/id_rsa.pub`

## Deploy

```bash
cd hetzner-iac

# 1. Create your variables file
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars — paste your hcloud_token, set SSH key paths

# 2. Initialize providers
tofu init

# 3. Preview
tofu plan

# 4. Apply (fully unattended — creates VM, converts to NixOS, configures everything)
tofu apply
```

After `apply` completes, Tofu prints the server IP and SSH command.

## Connect

```bash
# Use the output directly
$(tofu output -raw ssh_command)

# Or manually
ssh claude@<SERVER_IP>
```

## Spin Up Multiple VMs

To run several VMs in parallel, use Tofu workspaces:

```bash
tofu workspace new vm2
tofu apply

tofu workspace new vm3
tofu apply

# Switch between them
tofu workspace select vm2
tofu output ssh_command

# Destroy a specific VM
tofu workspace select vm3
tofu destroy
```

## Verify the Setup

```bash
ssh claude@<ip>

# Check KVM
ls /dev/kvm

# Check Android SDK
echo $ANDROID_HOME
adb --version

# Check whisper
which whisper
ls /var/lib/whisper/models/

# Check Claude Code
claude --version

# Check Java
java -version
```

## Update NixOS Config

If you change files in `nixos/`, re-apply on the VM:

```bash
# Option 1: Edit directly on the VM and rebuild
ssh claude@<ip>
sudo nano /root/studybuddy-nixos/configuration.nix
sudo nixos-rebuild switch --flake /root/studybuddy-nixos#studybuddy-dev

# Option 2: Taint and re-provision (recreates from scratch)
tofu taint null_resource.nixos_rebuild
tofu apply
```

## Roll Back

```bash
ssh claude@<ip>
sudo nixos-rebuild --rollback
```

## Destroy

```bash
tofu destroy
```

## File Structure

```
hetzner-iac/
├── main.tf                  # Provider, server, firewall, SSH key, nixos-rebuild provisioner
├── variables.tf             # Input variables (token, SSH keys, location, server type)
├── outputs.tf               # server_ip, ssh_command, nixos_config_path
├── terraform.tfvars.example # Sample variables file (no secrets)
├── provision.sh.tftpl       # User_data template: writes NixOS configs + runs nixos-infect
├── .gitignore               # Excludes tfstate, .terraform/, terraform.tfvars
└── README.md                # This file

nixos/                       # Standalone NixOS config (for manual deploy.sh use)
├── configuration.nix
├── users.nix
├── android.nix
├── whisper.nix
├── security.nix
├── flake.nix
├── flake.lock
├── deploy.sh
└── README.md
```

## Cost

The `ccx13` server costs approximately **€15.90/month** (billed hourly at ~€0.022/hr). Remember to `tofu destroy` when you're done to stop billing.

## Fixing Whisper Hashes

On first `nixos-rebuild`, Nix will error with the correct hashes for whisper.cpp source and model. SSH in, update the hashes in `/root/studybuddy-nixos/whisper.nix`, and re-run:

```bash
sudo nixos-rebuild switch --flake /root/studybuddy-nixos#studybuddy-dev
```

Then update `nixos/whisper.nix` and `hetzner-iac/provision.sh.tftpl` in the repo so future VMs get it right the first time.
