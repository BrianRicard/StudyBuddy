# StudyBuddy — Hetzner VM Infrastructure (nixos-anywhere)

OpenTofu IaC to provision a Hetzner Cloud VM running NixOS, pre-configured for Android CI, builds, emulator testing, and whisper.cpp.

Uses [nixos-anywhere](https://github.com/nix-community/nixos-anywhere) for clean, reproducible NixOS installation with declarative disk partitioning via [disko](https://github.com/nix-community/disko).

## How It Works

`tofu apply` is fully unattended — no manual steps after it completes:

1. Creates a Hetzner VM with Ubuntu 24.04 (any Linux works — nixos-anywhere doesn't care)
2. Runs `nixos-anywhere` locally which:
   - kexec boots a NixOS installer on the target
   - Partitions `/dev/sda` declaratively via disko (`nixos/disk-config.nix`)
   - Installs NixOS from `nixos/flake.nix`
   - Reboots into the finished system

After ~10–15 minutes, SSH in as `claude` and start working.

### Why nixos-anywhere?

The previous approach used `nixos-infect` (an in-place Ubuntu-to-NixOS conversion hack) which could leave artifacts and required copying `hardware-configuration.nix` from the VM. nixos-anywhere provides:

- **Clean install** — kexec into NixOS installer, no host distro leftovers
- **Declarative disk partitioning** — disko handles partitions, no manual `hardware-configuration.nix`
- **Single step** — one command does everything (partition, install, configure, reboot)
- **Any source distro** — works from any Linux with kexec support

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

> **Non-root user:** Claude Code refuses to run as root. All SSH access uses the `claude` user, which has passwordless sudo. Root SSH login is disabled after installation.

## Prerequisites

- **OpenTofu >= 1.6** — `brew install opentofu` or see [opentofu.org](https://opentofu.org/docs/intro/install/)
- **Nix with flakes** — required locally for nixos-anywhere (`curl -L https://nixos.org/nix/install | sh`)
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

# 4. Apply (fully unattended — creates VM, runs nixos-anywhere, configures everything)
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
sudo nixos-rebuild switch --flake /etc/nixos#studybuddy-dev

# Option 2: Re-run nixos-anywhere (wipes and reinstalls — use for major changes)
tofu taint null_resource.nixos_anywhere
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
├── main.tf                  # Provider, server, firewall, SSH key, nixos-anywhere provisioner
├── variables.tf             # Input variables (token, SSH keys, location, server type, config dir)
├── outputs.tf               # server_ip, ssh_command, deployment_method
├── terraform.tfvars.example # Sample variables file (no secrets)
├── .gitignore               # Excludes tfstate, .terraform/, terraform.tfvars
└── README.md                # This file

nixos/                       # NixOS configuration (used by both Tofu and standalone deploy)
├── configuration.nix        # Main system config (imports all modules)
├── users.nix                # User definitions (claude user, sudo)
├── android.nix              # Android SDK + emulator
├── whisper.nix              # whisper.cpp build + model
├── security.nix             # SSH hardening + fail2ban
├── flake.nix                # Nix flake (includes disko input)
├── flake.lock               # Locked dependency versions
├── disk-config.nix          # Disko declarative disk partitioning
├── deploy-anywhere.sh       # Standalone nixos-anywhere deploy script
├── deploy.sh                # Legacy nixos-infect deploy (kept for reference)
└── README.md
```

## Cost

The `ccx13` server costs approximately **€15.90/month** (billed hourly at ~€0.022/hr). Remember to `tofu destroy` when you're done to stop billing.

## Fixing Whisper Hashes

On first build, Nix will error with the correct hashes for whisper.cpp source and model. SSH in, update the hashes in `nixos/whisper.nix`, and re-run:

```bash
sudo nixos-rebuild switch --flake /etc/nixos#studybuddy-dev
```

Then update `nixos/whisper.nix` in the repo so future VMs get it right the first time.
