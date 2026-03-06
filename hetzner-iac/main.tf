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

resource "hcloud_server" "studybuddy" {
  name        = "studybuddy-dev"
  server_type = "cpx31"
  image       = "ubuntu-24.04"
  location    = var.server_location

  ssh_keys = [hcloud_ssh_key.studybuddy.id]

  user_data = templatefile("${path.module}/cloud-init.yaml", {
    ssh_public_key = trimspace(file(var.ssh_public_key_path))
  })

  firewall_ids = [hcloud_firewall.studybuddy.id]

  labels = {
    project = "studybuddy"
    env     = "dev"
  }
}
