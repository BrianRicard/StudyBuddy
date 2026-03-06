{ config, pkgs, ... }:

{
  imports = [
    # disk-config.nix and disko are loaded via flake.nix — no hardware-configuration.nix needed
    ./users.nix
    ./android.nix
    ./whisper.nix
    ./security.nix
  ];

  system.stateVersion = "24.05";

  # Enable flakes (required for `nixos-rebuild --flake`)
  nix.settings.experimental-features = [ "nix-command" "flakes" ];

  # Allow unfree packages (Android SDK components)
  nixpkgs.config.allowUnfree = true;

  # Bootloader: GRUB for BIOS (Hetzner Cloud default) with EFI fallback
  boot.loader.grub = {
    enable = true;
    efiSupport = true;
    efiInstallAsRemovable = true;
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
    gradle
    python3
    file
    tree
    ripgrep
    fd
    jq
  ];

  # Install Claude Code globally via npm activation script.
  # This runs once at system activation and is idempotent.
  system.activationScripts.claudeCode = {
    text = ''
      export HOME=/home/claude
      export NPM_CONFIG_PREFIX=/home/claude/.npm-global
      mkdir -p /home/claude/.npm-global/{bin,lib}
      if [ ! -f /home/claude/.npm-global/bin/claude ]; then
        ${pkgs.nodejs_20}/bin/npm install -g @anthropic-ai/claude-code 2>/dev/null || true
      fi
      chown -R claude:users /home/claude/.npm-global
    '';
    deps = [];
  };

  # Add npm global bin to system PATH
  environment.variables = {
    PATH = [ "/home/claude/.npm-global/bin" ];
  };

  # Shell profile to ensure npm global bin is on PATH for interactive sessions
  environment.etc."profile.d/npm-global.sh".text = ''
    export PATH="/home/claude/.npm-global/bin:$PATH"
  '';

  virtualisation.libvirtd.enable = true;
}
