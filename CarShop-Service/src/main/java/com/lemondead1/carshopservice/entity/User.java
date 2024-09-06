package com.lemondead1.carshopservice.entity;

import com.lemondead1.carshopservice.enums.UserRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.security.Password;

/**
 * Represents a user
 */
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode(callSuper = false)
public final class User extends UserPrincipal {
  private final int id;
  private final String username;
  private final String phoneNumber;
  private final String email;
  private final String password;
  private final UserRole role;
  private final int purchaseCount;

  /**
   * @param id            id
   * @param username      username
   * @param phoneNumber   phone number matching pattern \+?\d{8,13}
   * @param email         email
   * @param password      password
   * @param role          user role
   * @param purchaseCount number of purchase orders with status 'done'
   */
  public User(int id, String username, String phoneNumber, String email,
              String password, UserRole role, int purchaseCount) {
    super(username, new Password(password));
    this.id = id;
    this.username = username;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.password = password;
    this.role = role;
    this.purchaseCount = purchaseCount;
  }

  @Override
  public String getName() {
    return username;
  }
}
