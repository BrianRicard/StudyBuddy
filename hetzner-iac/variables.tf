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

variable "ssh_private_key_path" {
  description = "Path to the SSH private key for provisioning. Used by the null_resource to SSH in after nixos-infect reboot."
  type        = string
  default     = "~/.ssh/id_rsa"
}

variable "server_location" {
  description = "Hetzner datacenter location for the server."
  type        = string
  default     = "nbg1"
}

variable "server_type" {
  description = "Hetzner server type. Use ccx* for dedicated vCPU (KVM support). cx* is shared (no KVM)."
  type        = string
  default     = "ccx13"
}
