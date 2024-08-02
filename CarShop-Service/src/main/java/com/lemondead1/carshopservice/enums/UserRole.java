package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum UserRole implements HasId {
  ANONYMOUS("anonymous", "Anonymous"),
  CLIENT("client", "Client"),
  MANAGER("manager", "Manager"),
  ADMIN("admin", "Admin");

  public static final List<UserRole> ALL = List.of(values());

  private final String id;
  @Getter
  private final String prettyName;

  @Override
  public String getId() {
    return id;
  }
}
