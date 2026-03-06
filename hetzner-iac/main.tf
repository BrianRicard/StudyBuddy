terraform {
  required_version = ">= 1.6.0"

  required_providers {
    hcloud = {
      source  = "hetznercloud/hcloud"
      version = "~> 1.45"
    }
  }
}

provider "hcloud" {
  token = var.hcloud_token
}

# --- SSH Key ---

resource "hcloud_ssh_key" "studybuddy" {
  name       = "studybuddy-dev"
  public_key = file(var.ssh_public_key_path)
}

# --- Firewall ---

resource "hcloud_firewall" "studybuddy" {
  name = "studybuddy-dev-fw"

  # Allow SSH
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "22"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # Allow dev HTTP
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "8080"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # Allow all outbound TCP
  rule {
    direction       = "out"
    protocol        = "tcp"
    port            = "any"
    destination_ips = ["0.0.0.0/0", "::/0"]
  }

  # Allow all outbound UDP
  rule {
    direction       = "out"
    protocol        = "udp"
    port            = "any"
    destination_ips = ["0.0.0.0/0", "::/0"]
  }

  # Allow all outbound ICMP
  rule {
    direction       = "out"
    protocol        = "icmp"
    destination_ips = ["0.0.0.0/0", "::/0"]
  }
}

# --- Server ---
#
# nixos-anywhere deployment (replaces nixos-infect):
#   1. Create an Ubuntu VM (any Linux works — nixos-anywhere kexecs into NixOS installer)
#   2. Run nixos-anywhere locally which:
#      - kexec boots the NixOS installer on the target
#      - Partitions disk declaratively via disko (disk-config.nix)
#      - Installs NixOS from flake.nix
#      - Reboots into the finished system

resource "hcloud_server" "studybuddy" {
  name        = "studybuddy-dev"
  server_type = var.server_type
  image       = "ubuntu-24.04"
  location    = var.server_location

  ssh_keys = [hcloud_ssh_key.studybuddy.id]

  firewall_ids = [hcloud_firewall.studybuddy.id]

  labels = {
    project = "studybuddy"
    env     = "dev"
  }
}

# --- nixos-anywhere Deployment ---
#
# After the server is created, run nixos-anywhere from the local machine.
# This replaces the old two-phase approach (user_data nixos-infect + null_resource rebuild).
#
# nixos-anywhere will:
#   1. kexec into a NixOS installer (no distro dependency)
#   2. Partition /dev/sda via disko (disk-config.nix)
#   3. Install NixOS from the flake
#   4. Reboot into the finished system
#
# Requires: Nix installed locally with flakes enabled.

resource "null_resource" "nixos_anywhere" {
  depends_on = [hcloud_server.studybuddy]

  triggers = {
    server_id = hcloud_server.studybuddy.id
  }

  # Prepare config: inject SSH key into users.nix, then run nixos-anywhere
  provisioner "local-exec" {
    command = <<-SCRIPT
      set -euo pipefail

      NIXOS_DIR="${var.nixos_config_dir}"
      TARGET_IP="${hcloud_server.studybuddy.ipv4_address}"
      SSH_KEY_PATH="${var.ssh_private_key_path}"
      SSH_PUB_KEY=$(cat "${var.ssh_public_key_path}")

      # Prepare a temp copy with injected SSH key
      TMPDIR=$(mktemp -d)
      trap 'rm -rf "$TMPDIR"' EXIT
      cp -r "$NIXOS_DIR"/* "$TMPDIR/"

      # Inject SSH public key into users.nix
      ESCAPED_KEY=$(echo "$SSH_PUB_KEY" | sed 's/[&/\]/\\&/g')
      sed -i "s|@SSH_AUTHORIZED_KEY@|$ESCAPED_KEY|g" "$TMPDIR/users.nix"

      # Wait for SSH to become available on the fresh server
      echo "Waiting for SSH on $TARGET_IP..."
      for i in $(seq 1 30); do
        if ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
             -o ConnectTimeout=5 -i "$SSH_KEY_PATH" \
             "root@$TARGET_IP" true 2>/dev/null; then
          echo "SSH is up."
          break
        fi
        sleep 5
      done

      # Run nixos-anywhere
      echo "Running nixos-anywhere against $TARGET_IP..."
      nix run github:nix-community/nixos-anywhere -- \
        --flake "$TMPDIR#studybuddy-dev" \
        --target-host "root@$TARGET_IP" \
        --ssh-option "StrictHostKeyChecking=no" \
        --ssh-option "UserKnownHostsFile=/dev/null" \
        --ssh-option "IdentityFile=$SSH_KEY_PATH"

      echo "nixos-anywhere deployment complete."
    SCRIPT

    interpreter = ["bash", "-c"]
  }
}
