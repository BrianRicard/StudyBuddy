{ pkgs, ... }:

let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    cmdLineToolsVersion = "latest";
    platformVersions = [ "34" ];
    buildToolsVersions = [ "34.0.0" ];
    includeEmulator = true;
    includeSystemImages = true;
    systemImageTypes = [ "google_apis" ];
    abiVersions = [ "x86_64" ];
    includeNDK = false;
    useGoogleAPIs = true;
  };
  androidSdk = androidComposition.androidsdk;
in
{
  environment.systemPackages = [ androidSdk pkgs.android-tools ];

  environment.variables = {
    ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
    ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
  };

  # KVM for emulator acceleration.
  # NOTE: KVM acceleration requires a Hetzner CCX (dedicated vCPU) instance.
  # On shared CX instances the emulator will fall back to software rendering.
  boot.kernelModules = [ "kvm-intel" "kvm-amd" ];
}
