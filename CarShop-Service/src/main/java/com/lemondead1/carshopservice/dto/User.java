package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.UserRole;

public record User(int id, String username, String phoneNumber, String email, String password, UserRole role,
                   int purchaseCount) {
  public String prettyFormat() {
    var format = """
        user #%d named "%s" with phone number "%s" and email "%s" and %d purchases at role %s""";
    return String.format(format, id, username, phoneNumber, email, purchaseCount, role.getPrettyName());
  }
}
