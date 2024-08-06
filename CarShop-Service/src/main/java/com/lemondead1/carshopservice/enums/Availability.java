package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Availability implements HasId {
  AVAILABLE("available"),
  UNAVAILABLE("unavailable");

  private final String id;

  @Override
  public String getId() {
    return id;
  }
}
