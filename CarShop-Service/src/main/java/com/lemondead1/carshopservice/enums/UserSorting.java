package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import com.lemondead1.carshopservice.dto.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@RequiredArgsConstructor
public enum UserSorting implements HasId {
  USERNAME_DESC("username_reversed", Comparator.comparing(User::username, String::compareToIgnoreCase).reversed()),
  USERNAME_ASC("username", Comparator.comparing(User::username, String::compareToIgnoreCase)),
  ROLE_DESC("role", Comparator.comparing(User::role).reversed()),
  ROLE_ASC("role_reversed", Comparator.comparing(User::role));

  private final String id;
  @Getter
  private final Comparator<User> sorter;

  @Override
  public String getId() {
    return id;
  }
}
