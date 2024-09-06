package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum OrderState implements HasId {
  NEW("new"),
  PERFORMING("performing"),
  DONE("done"),
  CANCELLED("cancelled");

  public static final List<OrderState> ALL = List.of(values());
  public static final Set<OrderState> ALL_SET = Set.of(values());

  private static final Map<String, OrderState> idToEnum = Util.createIdToValueMap(values());

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }

  public static OrderState parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
