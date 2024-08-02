package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public enum OrderKind implements HasId {
  PURCHASE("purchase", "Purchase"),
  SERVICE("service", "Service");

  private final String id;
  private final String prettyName;

  OrderKind(String id, String prettyName) {
    this.id = id;
    this.prettyName = prettyName;
  }

  @Override
  public String getId() {
    return id;
  }

  public String getPrettyName() {
    return prettyName;
  }
}
