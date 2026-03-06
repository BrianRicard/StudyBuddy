{ config, pkgs, ... }:

{
  imports = [
    ./hardware-configuration.nix # generated on the VM by nixos-infect
    ./users.nix
    ./android.nix
    ./whisper.nix
    ./security.nix
  ];

  system.stateVersion = "24.05";

  boot.loader.grub = {
    enable = true;
    device = "/dev/sda";
  };

  networking = {
    hostName = "studybuddy-dev";
    firewall = {
      enable = true;
      allowedTCPPorts = [ 22 8080 ];
    };
  };

  time.timeZone = "America/Toronto";
  i18n.defaultLocale = "en_CA.UTF-8";

  # Core packages available system-wide
  environment.systemPackages = with pkgs; [
    git
    curl
    wget
    unzip
    zip
    htop
    tmux
    cmake
    ninja
    pkg-config
    jdk17
    nodejs_20
    nodePackages.npm
    gradle
    python3
    file
    tree
  ];

  # Node.js global packages (Claude Code)
  programs.npm = {
    enable = true;
    npmrc = ''
      prefix = /home/claude/.npm-global
    '';
  };

  virtualisation.libvirtd.enable = true;
}
