package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum OrderKind implements HasId {
  PURCHASE("purchase"),
  SERVICE("service");

  public static final List<OrderKind> ALL = List.of(values());
  public static final Set<OrderKind> ALL_SET = Set.of(values());

  private static final Map<String, OrderKind> idToEnum = Util.createIdToValueMap(values());

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }

  public static OrderKind parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
