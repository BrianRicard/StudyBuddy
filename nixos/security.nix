{ ... }:

{
  services.openssh = {
    enable = true;
    settings = {
      PermitRootLogin = "no";
      PasswordAuthentication = "no";
      KbdInteractiveAuthentication = false;
    };
  };

  # fail2ban for SSH brute force protection
  services.fail2ban = {
    enable = true;
    maxretry = 5;
    bantime = "1h";
  };
}
