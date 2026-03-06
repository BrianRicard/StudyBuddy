{ pkgs, ... }:

let
  whisper-cpp = pkgs.stdenv.mkDerivation {
    pname = "whisper-cpp";
    version = "latest";
    src = pkgs.fetchFromGitHub {
      owner = "ggerganov";
      repo = "whisper.cpp";
      rev = "master";
      sha256 = pkgs.lib.fakeSha256; # Replace with actual hash after first build
    };
    nativeBuildInputs = with pkgs; [ cmake ninja ];
    cmakeFlags = [ "-DWHISPER_AVX2=ON" ];
    installPhase = ''
      mkdir -p $out/bin $out/models
      cp main $out/bin/whisper
    '';
  };
in
{
  environment.systemPackages = [ whisper-cpp ];

  # Download base model via activation script
  system.activationScripts.whisperModel = {
    text = ''
      MODEL_PATH=/var/lib/whisper/models/ggml-base.bin
      if [ ! -f "$MODEL_PATH" ]; then
        mkdir -p /var/lib/whisper/models
        ${pkgs.curl}/bin/curl -L \
          "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin" \
          -o "$MODEL_PATH"
      fi
    '';
    deps = [];
  };
}
