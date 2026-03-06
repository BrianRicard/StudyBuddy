output "server_ip" {
  description = "Public IPv4 address of the studybuddy-dev server."
  value       = hcloud_server.studybuddy.ipv4_address
}

output "ssh_command" {
  description = "Ready-to-use SSH command to connect to the server."
  value       = "ssh claude@${hcloud_server.studybuddy.ipv4_address}"
}

output "nixos_config_path" {
  description = "Path to the NixOS configuration on the VM."
  value       = "/root/studybuddy-nixos"
}
