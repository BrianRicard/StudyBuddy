# StudyBuddy — Hetzner VM Infrastructure (nixos-anywhere)

OpenTofu IaC to provision a Hetzner Cloud VM, then install NixOS via [nixos-anywhere](https://github.com/nix-community/nixos-anywhere) with declarative disk partitioning via [disko](https://github.com/nix-community/disko).

## How It Works

Two steps — Terraform creates the VM, then you run nixos-anywhere:

1. **`tofu apply`** — Creates a Hetzner VM (Ubuntu 24.04), firewall, and SSH key
2. **`deploy-anywhere.sh`** — Runs nixos-anywhere which:
   - kexec boots a NixOS installer on the target
   - Partitions `/dev/sda` declaratively via disko (`nixos/disk-config.nix`)
   - Installs NixOS from `nixos/flake.nix`
   - Reboots into the finished system

The nixos-anywhere step is separate from Terraform so it works cross-platform — run it from **WSL**, **Git Bash**, or any shell with Nix installed.

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
- **Nix with flakes** — required for nixos-anywhere (run from WSL/Git Bash on Windows)
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

# 4. Create the VM
tofu apply

# 5. Install NixOS (run from a shell with Nix — WSL, Git Bash, Linux, macOS)
#    The exact command is printed by tofu as the "nixos_anywhere_command" output:
../nixos/deploy-anywhere.sh <SERVER_IP> ~/.ssh/your_key.pub

# Or copy the command from tofu output:
$(tofu output -raw nixos_anywhere_command)
```

After nixos-anywhere completes (~10–15 min), SSH in as `claude`.

## Windows Users

Terraform/OpenTofu runs natively on Windows. The nixos-anywhere step requires Nix, which runs in **WSL**:

```powershell
# Step 1: From PowerShell / cmd — create the VM
tofu apply

# Step 2: From WSL — install NixOS
wsl ../nixos/deploy-anywhere.sh <SERVER_IP> ~/.ssh/your_key.pub
```

## Connect

```bash
# Use the output directly
$(tofu output -raw ssh_command)

# Or manually
ssh claude@<SERVER_IP>
```

## Spin Up Multiple VMs

```bash
tofu workspace new vm2
tofu apply
# then run deploy-anywhere.sh against the new IP

tofu workspace select vm2
tofu output ssh_command
```

## Verify the Setup

```bash
ssh claude@<ip>
java -version               # JDK 17
echo $ANDROID_HOME           # Android SDK
node --version               # Node.js 20
claude --version             # Claude Code
ls /dev/kvm                  # KVM support
```

## Update NixOS Config

```bash
# Edit directly on the VM and rebuild
ssh claude@<ip>
sudo nixos-rebuild switch --flake /etc/nixos#studybuddy-dev

# Or re-run nixos-anywhere for a clean reinstall
../nixos/deploy-anywhere.sh <SERVER_IP> ~/.ssh/your_key.pub
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
├── main.tf                  # Provider, server, firewall, SSH key
├── variables.tf             # Input variables (token, SSH keys, location, server type)
├── outputs.tf               # server_ip, ssh_command, nixos_anywhere_command
├── terraform.tfvars.example # Sample variables file (no secrets)
├── .gitignore               # Excludes tfstate, .terraform/, terraform.tfvars
└── README.md                # This file

nixos/                       # NixOS configuration (used by deploy-anywhere.sh)
├── configuration.nix        # Main system config
├── disk-config.nix          # Disko declarative disk partitioning
├── flake.nix                # Nix flake (includes disko input)
├── deploy-anywhere.sh       # nixos-anywhere deploy script
├── deploy.sh                # Legacy nixos-infect deploy (reference only)
└── ...
```

## Cost

The `ccx13` server costs approximately **€15.90/month** (~€0.022/hr). Remember to `tofu destroy` when done.

## Fixing Whisper Hashes

On first build, Nix errors with the correct hashes. Update them in `nixos/whisper.nix` and rebuild:

```bash
sudo nixos-rebuild switch --flake /etc/nixos#studybuddy-dev
```
