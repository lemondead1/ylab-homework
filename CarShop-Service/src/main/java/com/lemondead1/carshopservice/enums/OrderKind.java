package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderKind implements HasId {
  PURCHASE("purchase", "Purchase"),
  SERVICE("service", "Service");

  private final String id;
  @Getter
  private final String prettyName;

  @Override
  public String getId() {
    return id;
  }

}
