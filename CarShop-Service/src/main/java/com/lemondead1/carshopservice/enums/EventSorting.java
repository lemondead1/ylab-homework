package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventSorting implements HasId {
  TIMESTAMP_DESC("latest_first"),
  TIMESTAMP_ASC("older_first"),
  USERNAME_ASC("username"),
  USERNAME_DESC("username_reversed"),
  TYPE_ASC("type"),
  TYPE_DESC("type_reversed");

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }
}
