package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;

import java.util.List;

public enum OrderState implements HasId {
  NEW("new", "New"),
  PERFORMING("performing", "Performing"),
  DONE("done", "Done"),
  CANCELLED("cancelled", "Cancelled");

  public static final List<OrderState> ALL = List.of(values());

  private final String id;
  @Getter
  private final String prettyName;

  OrderState(String id, String prettyName) {
    this.id = id;
    this.prettyName = prettyName;
  }

  @Override
  public String getId() {
    return id;
  }

}
