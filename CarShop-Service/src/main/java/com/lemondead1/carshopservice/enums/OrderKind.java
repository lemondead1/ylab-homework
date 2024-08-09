package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum OrderKind implements HasId {
  PURCHASE("purchase", "Purchase"),
  SERVICE("service", "Service");

  public static final List<OrderKind> ALL = List.of(values());

  private final String id;
  @Getter
  private final String prettyName;

  @Override
  public String getId() {
    return id;
  }

  public static OrderKind parse(String id) {
    return switch (id) {
      case "purchase" -> PURCHASE;
      case "service" -> SERVICE;
      default -> throw new IllegalArgumentException();
    };
  }
}
