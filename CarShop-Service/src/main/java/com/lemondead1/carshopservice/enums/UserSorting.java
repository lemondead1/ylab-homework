package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserSorting implements HasId {
  USERNAME_DESC("username"),
  USERNAME_ASC("username_reversed"),
  ROLE_DESC("role"),
  ROLE_ASC("role_reversed");

  private final String id;

  @Override
  public String getId() {
    return id;
  }
}
