variable "hcloud_token" {
  description = "Hetzner Cloud API token. Set via TF_VAR_hcloud_token or terraform.tfvars."
  type        = string
  sensitive   = true
}

variable "ssh_public_key_path" {
  description = "Path to the SSH public key to install on the server."
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "anthropic_api_key" {
  description = "Anthropic API key for Claude Code. Set via TF_VAR_anthropic_api_key or terraform.tfvars."
  type        = string
  sensitive   = true
  default     = ""
}

variable "server_location" {
  description = "Hetzner datacenter location for the server."
  type        = string
  default     = "nbg1"
}
