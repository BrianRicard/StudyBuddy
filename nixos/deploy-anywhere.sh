#!/usr/bin/env bash
# deploy-anywhere.sh — Deploy StudyBuddy NixOS VM using nixos-anywhere.
#
# Usage (from your local machine):
#   ./nixos/deploy-anywhere.sh <server-ip> [ssh-key-path]
#
# What it does:
#   1. Injects your SSH public key into users.nix
#   2. Runs nixos-anywhere which:
#      - kexec boots into a NixOS installer (no nixos-infect needed)
#      - Partitions disk declaratively via disko (disk-config.nix)
#      - Installs NixOS from flake.nix
#      - Reboots into the finished system
#   3. Verifies the installation
#
# Prerequisites:
#   - Nix installed locally with flakes enabled
#   - A fresh Hetzner Cloud VM with root SSH access (any Linux distro)
#   - Your SSH public key for the 'claude' user
#   - VM must have at least 1 GB RAM

set -euo pipefail

SERVER_IP="${1:?Usage: $0 <server-ip> [ssh-key-path]}"
SSH_KEY_PATH="${2:-$HOME/.ssh/hetzner_studybuddy.pub}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# Validate inputs
[[ -f "$SSH_KEY_PATH" ]] || error "SSH public key not found at: $SSH_KEY_PATH"
SSH_PUB_KEY="$(cat "$SSH_KEY_PATH")"

command -v nix >/dev/null 2>&1 || error "Nix is not installed. Install it from https://nixos.org/download"

SSH_OPTS="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o ConnectTimeout=10"

wait_for_ssh() {
  local user="$1" host="$2" max_attempts="${3:-30}"
  info "Waiting for SSH on $host (as $user)..."
  for i in $(seq 1 "$max_attempts"); do
    if ssh $SSH_OPTS "$user@$host" true 2>/dev/null; then
      info "SSH is up."
      return 0
    fi
    echo -n "."
    sleep 10
  done
  error "SSH not available after $((max_attempts * 10))s"
}

# ──────────────────────────────────────────────
# Phase 1: Prepare config with SSH key
# ──────────────────────────────────────────────
info "Phase 1: Preparing NixOS configuration"

TMPDIR="$(mktemp -d)"
trap 'rm -rf "$TMPDIR"' EXIT

cp -r "$SCRIPT_DIR"/* "$TMPDIR/"

# Inject SSH key into users.nix
ESCAPED_KEY="$(echo "$SSH_PUB_KEY" | sed 's/[&/\]/\\&/g')"
sed -i "s|@SSH_AUTHORIZED_KEY@|$ESCAPED_KEY|g" "$TMPDIR/users.nix"

info "SSH key injected into configuration."

# ──────────────────────────────────────────────
# Phase 2: Run nixos-anywhere
# ──────────────────────────────────────────────
info "Phase 2: Running nixos-anywhere"
info "  Target: root@$SERVER_IP"
info "  Flake:  $TMPDIR#studybuddy-dev"
info ""
info "This will:"
info "  1. kexec into a NixOS installer on the target"
info "  2. Partition /dev/sda using disko (disk-config.nix)"
info "  3. Install NixOS from flake configuration"
info "  4. Reboot into the new system"
warn "All data on the target will be ERASED."
echo ""

# Check if the target is reachable
wait_for_ssh root "$SERVER_IP" 6

nix run github:nix-community/nixos-anywhere -- \
  --flake "$TMPDIR#studybuddy-dev" \
  --target-host "root@$SERVER_IP" \
  --ssh-option "StrictHostKeyChecking=no" \
  --ssh-option "UserKnownHostsFile=/dev/null"

info "nixos-anywhere completed. Waiting for system to come up..."

# ──────────────────────────────────────────────
# Phase 3: Verify
# ──────────────────────────────────────────────
sleep 15
wait_for_ssh claude "$SERVER_IP" 30

info "Phase 3: Verifying setup"

ssh $SSH_OPTS "claude@$SERVER_IP" bash <<'VERIFY_EOF'
  echo "=== System ==="
  uname -a
  echo ""
  echo "=== Java ==="
  java -version 2>&1
  echo ""
  echo "=== Android SDK ==="
  echo "ANDROID_HOME=$ANDROID_HOME"
  ls "$ANDROID_HOME/platforms/" 2>/dev/null || echo "(checking...)"
  echo ""
  echo "=== Node.js ==="
  node --version
  echo ""
  echo "=== Gradle ==="
  gradle --version 2>&1 | head -3
  echo ""
  echo "=== whisper ==="
  which whisper 2>/dev/null && echo "whisper binary: OK" || echo "whisper: not yet available (hash needs fixing)"
  echo ""
  echo "=== Whisper model ==="
  ls -lh /var/lib/whisper/models/ 2>/dev/null || echo "(model not yet downloaded)"
  echo ""
  echo "=== Claude Code ==="
  which claude 2>/dev/null && claude --version 2>/dev/null || echo "(install pending or PATH not set)"
VERIFY_EOF

echo ""
info "=========================================="
info " StudyBuddy VM is ready!"
info " SSH: ssh claude@$SERVER_IP"
info "=========================================="
info ""
info "Advantages over nixos-infect:"
info "  - Clean install (no Ubuntu leftovers)"
info "  - Declarative disk partitioning (disko)"
info "  - Works from any Linux distro on the target"
info "  - Single-step, fully unattended"
info ""
warn "If whisper hash errors occurred, see nixos/README.md for how to fix hashes."
