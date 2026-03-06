{ pkgs, ... }:

{
  users.users.claude = {
    isNormalUser = true;
    shell = pkgs.bash;
    extraGroups = [ "wheel" "kvm" "libvirtd" "audio" "video" ];
    openssh.authorizedKeys.keys = [
      # Paste your ~/.ssh/hetzner_studybuddy.pub content here
      "ssh-ed25519 REPLACE_WITH_YOUR_PUBLIC_KEY studybuddy-hetzner"
    ];
  };

  # Passwordless sudo for wheel group
  security.sudo.wheelNeedsPassword = false;
}
