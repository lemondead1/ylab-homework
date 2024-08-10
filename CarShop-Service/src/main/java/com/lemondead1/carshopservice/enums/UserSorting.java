package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
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
}
