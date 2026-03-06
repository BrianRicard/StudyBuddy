{ pkgs, ... }:

let
  # SSH public keys for the claude user.
  # Option 1: Paste keys directly into this list.
  # Option 2: Use the deploy script which injects keys via sed before nixos-rebuild.
  sshKeys = [
    # Paste your ~/.ssh/hetzner_studybuddy.pub content here, or use deploy.sh
    # which replaces this placeholder automatically.
    "@SSH_AUTHORIZED_KEY@"
  ];

  # Filter out the placeholder if it was never replaced
  validKeys = builtins.filter (k: k != "@SSH_AUTHORIZED_KEY@") sshKeys;
in
{
  users.users.claude = {
    isNormalUser = true;
    shell = pkgs.bash;
    extraGroups = [ "wheel" "kvm" "libvirtd" "audio" "video" ];
    openssh.authorizedKeys.keys = validKeys;
    # Ensure home directory exists with correct permissions
    createHome = true;
    home = "/home/claude";
  };

  # Passwordless sudo for wheel group
  security.sudo.wheelNeedsPassword = false;
}
