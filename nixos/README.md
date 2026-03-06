# StudyBuddy NixOS VM Configuration

Declarative NixOS configuration for the StudyBuddy Hetzner Cloud development VM.
Provides an Android build/CI/test environment with emulator support, whisper.cpp,
and SSH hardening.

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

## First-Time Setup

After `nixos-infect` converts the VM:

```bash
# 1. Clone this repo onto the VM
git clone <your-repo> ~/nixos-config
cd ~/nixos-config/nixos

# 2. Copy generated hardware config
cp /etc/nixos/hardware-configuration.nix ./

# 3. Paste your SSH public key into users.nix

# 4. Apply the configuration
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

## Notes

- **KVM acceleration** requires a Hetzner CCX (dedicated vCPU) instance.
  On shared CX instances the Android emulator falls back to software rendering.
- **whisper.cpp** builds from source; the `sha256` in `whisper.nix` must be
  replaced with the actual hash after the first build attempt.
- The `hardware-configuration.nix` file is **not** checked in — it is generated
  per-VM by `nixos-infect` or `nixos-generate-config`.
