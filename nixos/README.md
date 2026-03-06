# StudyBuddy NixOS VM Configuration

Declarative NixOS configuration for the StudyBuddy Hetzner Cloud development VM.
Provides an Android build/CI/test environment with emulator support, whisper.cpp,
and SSH hardening.

## What Gets Installed

| Component | Details |
|---|---|
| **JDK 17** | For Gradle / Android builds |
| **Android SDK** | Platform 34, build-tools 34.0.0, x86_64 emulator, licenses auto-accepted |
| **Node.js 20** | For Claude Code |
| **Claude Code** | Installed globally via npm activation script |
| **Gradle** | System-wide for builds |
| **whisper.cpp** | Built from source with AVX2, base model auto-downloaded |
| **KVM** | Emulator acceleration (requires CCX dedicated vCPU instance) |
| **fail2ban** | SSH brute force protection |
| **libvirtd** | VM management |

## File Structure

| File | Purpose |
|---|---|
| `configuration.nix` | Main system config (imports all modules) |
| `users.nix` | User definitions (claude user, sudo) |
| `android.nix` | Android SDK + emulator via `androidenv` |
| `whisper.nix` | whisper.cpp build from source + model download |
| `security.nix` | SSH hardening + fail2ban |
| `flake.nix` | Nix flake (includes disko for declarative disk partitioning) |
| `flake.lock` | Locked dependency versions (populated by nix) |
| `disk-config.nix` | Disko disk partitioning layout (GPT, boot, root) |
| `deploy-anywhere.sh` | Deploy via nixos-anywhere (recommended) |
| `deploy.sh` | Legacy deploy via nixos-infect (kept for reference) |

## Deployment with nixos-anywhere (Recommended)

Uses [nixos-anywhere](https://github.com/nix-community/nixos-anywhere) for a clean,
single-command install. No nixos-infect, no Ubuntu leftovers, declarative disk partitioning.

```bash
./nixos/deploy-anywhere.sh <server-ip> [~/.ssh/hetzner_studybuddy.pub]
```

This will:
1. kexec boot a NixOS installer on the target (works from any Linux distro)
2. Partition `/dev/sda` declaratively via disko (`disk-config.nix`)
3. Install NixOS from the flake configuration
4. Reboot into the finished system
5. Verify all components

After ~10–15 minutes, SSH in as `claude@<server-ip>` and start working.

### Prerequisites

- **Nix** installed locally with flakes enabled
- A fresh Hetzner Cloud VM (any Linux distro — Ubuntu, Debian, etc.)
- Root SSH access (Hetzner default)
- Your SSH public key at `~/.ssh/hetzner_studybuddy.pub`
- Target VM must have at least 1 GB RAM

### Why nixos-anywhere over nixos-infect?

| | nixos-infect (legacy) | nixos-anywhere |
|---|---|---|
| **Install method** | In-place conversion hack | Clean kexec → NixOS installer |
| **Disk partitioning** | Inherits existing partitions | Declarative via disko |
| **Leftover artifacts** | May leave Ubuntu remnants | Clean install, nothing left over |
| **hardware-configuration.nix** | Must copy from VM | Not needed (disko handles it) |
| **Reproducibility** | Depends on host distro state | Fully reproducible |
| **Target distro** | Ubuntu/Debian only | Any Linux with kexec (most x86_64) |

## Legacy Deployment (nixos-infect)

The old `deploy.sh` is kept for reference but is no longer recommended:

```bash
./nixos/deploy.sh <server-ip> [~/.ssh/hetzner_studybuddy.pub]
```

## Day-to-Day Usage

```bash
# Make a config change, then apply it
sudo nixos-rebuild switch --flake .#studybuddy-dev

# Test a change without making it permanent
sudo nixos-rebuild test --flake .#studybuddy-dev

# Roll back to previous generation
sudo nixos-rebuild --rollback

# List all generations
sudo nix-env --list-generations --profile /nix/var/nix/profiles/system
```

## Upgrading

```bash
nix flake update          # update flake.lock to latest nixpkgs
sudo nixos-rebuild switch --flake .#studybuddy-dev
```

## Fixing Whisper Hashes

The `whisper.nix` file uses placeholder hashes that must be replaced on first build.
Nix will error with the expected hash — just copy it in:

```
error: hash mismatch in fixed-output derivation:
  specified: sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=
  got:       sha256-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx=
```

Replace both `sha256-AAA...` values in `whisper.nix` with the real hashes, then
re-run `sudo nixos-rebuild switch --flake .#studybuddy-dev`.

Alternatively, get the hashes upfront:

```bash
# whisper.cpp source hash
nix-prefetch-url --unpack https://github.com/ggerganov/whisper.cpp/archive/v1.7.3.tar.gz

# whisper model hash
nix-prefetch-url https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin
```

## Notes

- **KVM acceleration** requires a Hetzner CCX (dedicated vCPU) instance.
  On shared CX instances the Android emulator falls back to software rendering.
- **No hardware-configuration.nix needed** — disko handles disk setup declaratively
  via `disk-config.nix`. This file is loaded through the flake.
- All Android SDK licenses are accepted in `android.nix` for unattended builds.
- The `claude` user has passwordless sudo via the `wheel` group.
