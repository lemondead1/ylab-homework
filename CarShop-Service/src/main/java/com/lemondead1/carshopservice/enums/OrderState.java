package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public enum OrderState implements HasId {
  NEW("new", "New"),
  PERFORMING("performing", "Performing"),
  DONE("done", "Done"),
  CANCELLED("cancelled", "Cancelled");

  public static final List<OrderState> ALL = List.of(values());
  public static final Set<OrderState> ALL_SET = Set.of(values());

  private final String id;
  @Getter
  private final String prettyName;

  @Override
  public String getId() {
    return id;
  }

  public static OrderState parse(String id) {
    return switch (id) {
      case "new" -> NEW;
      case "performing" -> PERFORMING;
      case "done" -> DONE;
      case "cancelled" -> CANCELLED;
      default -> throw new IllegalArgumentException();
    };
  }
}
