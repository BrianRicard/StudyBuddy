{ pkgs, ... }:

let
  # Pin to a specific release for reproducibility.
  # To update: change rev + hash. Get the new hash with:
  #   nix-prefetch-url --unpack https://github.com/ggerganov/whisper.cpp/archive/<new-tag>.tar.gz
  whisper-cpp = pkgs.stdenv.mkDerivation rec {
    pname = "whisper-cpp";
    version = "1.7.3";
    src = pkgs.fetchFromGitHub {
      owner = "ggerganov";
      repo = "whisper.cpp";
      rev = "v${version}";
      # After first build: nix will show the expected hash in the error message.
      # Replace this value with that hash.
      hash = "sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    };
    nativeBuildInputs = with pkgs; [ cmake ninja pkg-config ];
    cmakeFlags = [
      "-DWHISPER_AVX2=ON"
      "-DCMAKE_BUILD_TYPE=Release"
    ];
    installPhase = ''
      mkdir -p $out/bin
      cp bin/main $out/bin/whisper || cp main $out/bin/whisper
    '';
    meta = with pkgs.lib; {
      description = "Port of OpenAI's Whisper model in C/C++";
      homepage = "https://github.com/ggerganov/whisper.cpp";
      license = licenses.mit;
    };
  };

  # Pre-download the whisper base model as a fixed-output derivation.
  # This makes it part of the Nix store (cached, reproducible).
  whisper-model-base = pkgs.fetchurl {
    url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin";
    # Same approach: first build will error with the real hash. Replace then.
    hash = "sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
  };
in
{
  environment.systemPackages = [ whisper-cpp ];

  # Symlink the model into a well-known path so scripts can find it.
  system.activationScripts.whisperModel = {
    text = ''
      mkdir -p /var/lib/whisper/models
      ln -sf ${whisper-model-base} /var/lib/whisper/models/ggml-base.bin
    '';
    deps = [];
  };
}
