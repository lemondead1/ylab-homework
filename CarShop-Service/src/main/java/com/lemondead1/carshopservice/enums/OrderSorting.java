package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public enum OrderSorting implements HasId {
  LATEST_FIRST("latest_first"),
  OLDEST_FIRST("oldest_first"),
  CAR_NAME_ALPHABETICALLY("");

  private final String id;

  OrderSorting(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
