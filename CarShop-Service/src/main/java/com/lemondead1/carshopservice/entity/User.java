package com.lemondead1.carshopservice.entity;

import com.lemondead1.carshopservice.enums.UserRole;

/**
 * Represents a user
 *
 * @param id id
 * @param username username
 * @param phoneNumber phone number matching pattern \+?\d{8,13}
 * @param email email
 * @param password password
 * @param role user role
 * @param purchaseCount number of purchase orders with status 'done'
 */
public record User(int id, String username, String phoneNumber, String email, String password, UserRole role,
                   int purchaseCount) {
  public String prettyFormat() {
    var format = """
        user #%d named "%s" with phone number "%s" and email "%s" and %d purchases on role %s""";
    return String.format(format, id, username, phoneNumber, email, purchaseCount, role.getPrettyName());
  }
}
