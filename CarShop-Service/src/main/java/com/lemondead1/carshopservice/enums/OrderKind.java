package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public enum OrderKind implements HasId {
  PURCHASE("purchase"),
  SERVICE("service");

  private final String id;

  OrderKind(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
