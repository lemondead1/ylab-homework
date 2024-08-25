package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CarSorting implements HasId {
  NAME_ASC("name"),
  NAME_DESC("name_reversed"),
  PRODUCTION_YEAR_ASC("older_first"),
  PRODUCTION_YEAR_DESC("newer_first"),
  PRICE_ASC("cheaper_first"),
  PRICE_DESC("expensive_first");

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }
}
