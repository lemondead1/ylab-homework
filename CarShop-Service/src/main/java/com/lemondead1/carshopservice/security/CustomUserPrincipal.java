package com.lemondead1.carshopservice.security;

import com.lemondead1.carshopservice.enums.UserRole;
import lombok.*;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.security.Password;

@Getter
public class CustomUserPrincipal extends UserPrincipal {
  private final int id;
  private final String username;
  private final String phoneNumber;
  private final String email;
  private final String password;
  private final UserRole role;

  public CustomUserPrincipal(int id, String username, String phoneNumber, String email, String password, UserRole role) {
    super(username, new Password(password));
    this.id = id;
    this.username = username;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.password = password;
    this.role = role;
  }
}
