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
| `flake.nix` | Nix flake for reproducible builds |
| `flake.lock` | Locked dependency versions (populated by nix) |
| `deploy.sh` | Zero-touch provisioning script |

## Zero-Touch Deployment

Spin up a fresh Hetzner VM and run one command from your local machine:

```bash
./nixos/deploy.sh <server-ip> [~/.ssh/hetzner_studybuddy.pub]
```

This will:
1. Run `nixos-infect` to convert Ubuntu/Debian to NixOS
2. Copy this configuration to the VM
3. Inject your SSH public key
4. Run `nixos-rebuild switch`
5. Verify all components

After ~10–15 minutes, SSH in as `claude@<server-ip>` and start working.

### Prerequisites

- A fresh Hetzner Cloud VM (Ubuntu 22.04 or Debian 12)
- Root SSH access (Hetzner default)
- Your SSH public key at `~/.ssh/hetzner_studybuddy.pub`

## Manual Setup (Alternative)

If you prefer to set up manually:

```bash
# 1. SSH into the VM as root and run nixos-infect
curl -fsSL https://raw.githubusercontent.com/elitak/nixos-infect/master/nixos-infect | \
  NIX_CHANNEL=nixos-24.05 bash -x

# 2. After reboot, clone this repo
git clone <your-repo> ~/nixos-config
cd ~/nixos-config/nixos

# 3. Copy generated hardware config
cp /etc/nixos/hardware-configuration.nix ./

# 4. Edit users.nix — replace @SSH_AUTHORIZED_KEY@ with your public key

# 5. Apply the configuration
sudo nixos-rebuild switch --flake .#studybuddy-dev
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
- The `hardware-configuration.nix` file is **not** checked in — it is generated
  per-VM by `nixos-infect` or `nixos-generate-config`.
- All Android SDK licenses are accepted in `android.nix` for unattended builds.
- The `claude` user has passwordless sudo via the `wheel` group.
