package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@Schema(description = """
    Sort order:
     * `username` - Sorted by username, from A to Z
     * `username_reversed` - Sorted by username, from Z to A
     * `email` - Sorted by email, from A to Z
     * `email_reversed` - Sorted by email, from Z to A
     * `role` - Sorted by role in order: `client`, `manager`, `admin`
     * `role_reversed` - Sorted by role in order: `admin`, `manager`, `client`
     * `more_purchases_first` - Sorted by purchase count in descending order
     * `fewer_purchases_first` - Sorted by purchase count in ascending order""")
@RequiredArgsConstructor
public enum UserSorting implements HasId {
  USERNAME_DESC("username_reversed"),
  USERNAME_ASC("username"),
  EMAIL_DESC("email_reversed"),
  EMAIL_ASC("email"),
  ROLE_DESC("role"),
  ROLE_ASC("role_reversed"),
  PURCHASES_DESC("more_purchases_first"),
  PURCHASES_ASC("less_purchases_first");

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }
}
