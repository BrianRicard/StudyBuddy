output "server_ip" {
  description = "Public IPv4 address of the studybuddy-dev server."
  value       = hcloud_server.studybuddy.ipv4_address
}

output "ssh_command" {
  description = "Ready-to-use SSH command to connect to the server."
  value       = "ssh claude@${hcloud_server.studybuddy.ipv4_address}"
}

output "nixos_anywhere_command" {
  description = "Run this command (from WSL, Git Bash, or a Linux/macOS shell with Nix) to install NixOS on the server."
  value       = "../nixos/deploy-anywhere.sh ${hcloud_server.studybuddy.ipv4_address} ${var.ssh_public_key_path}"
}

output "deployment_method" {
  description = "Deployment method used for NixOS installation."
  value       = "nixos-anywhere (disko + kexec) — run nixos_anywhere_command after apply"
}
