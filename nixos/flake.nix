{
  description = "StudyBuddy Dev VM — NixOS configuration (nixos-anywhere + disko)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    nixpkgs-unstable.url = "github:NixOS/nixpkgs/nixos-unstable";

    disko = {
      url = "github:nix-community/disko";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, nixpkgs-unstable, disko, ... }:
  let
    system = "x86_64-linux";
    # Make unstable packages available as pkgs.unstable.*
    overlay-unstable = final: prev: {
      unstable = import nixpkgs-unstable {
        inherit system;
        config.allowUnfree = true;
      };
    };
  in
  {
    nixosConfigurations.studybuddy-dev = nixpkgs.lib.nixosSystem {
      inherit system;
      modules = [
        ({ ... }: { nixpkgs.overlays = [ overlay-unstable ]; })
        disko.nixosModules.disko
        ./disk-config.nix
        ./configuration.nix
      ];
    };
  };
}
