package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Availability implements HasId {
  AVAILABLE("available"),
  UNAVAILABLE("unavailable");

  private final String id;
}
