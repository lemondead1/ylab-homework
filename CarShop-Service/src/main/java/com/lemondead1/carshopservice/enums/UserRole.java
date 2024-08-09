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

  public static final List<UserRole> AUTHORIZED = List.of(CLIENT, MANAGER, ADMIN);

  private final String id;
  @Getter
  private final String prettyName;

  @Override
  public String getId() {
    return id;
  }

  public static UserRole parse(String id) {
    return switch (id) {
      case "anonymous" -> ANONYMOUS;
      case "client" -> CLIENT;
      case "manager" -> MANAGER;
      case "admin" -> ADMIN;
      default -> throw new IllegalArgumentException();
    };
  }
}
