package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole implements HasId {
  ANONYMOUS("anonymous"),
  CLIENT("client"),
  MANAGER("manager"),
  ADMIN("admin");

  private final String id;

  @Override
  public String getId() {
    return id;
  }
}
