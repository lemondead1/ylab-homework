package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum OrderKind implements HasId {
  PURCHASE("purchase", "Purchase"),
  SERVICE("service", "Service");

  public static final List<OrderKind> ALL = List.of(values());
  public static final Set<OrderKind> ALL_SET = Set.of(values());

  private static final Map<String, OrderKind> idToEnum = Util.createIdToValueMap(values());

  private final String id;
  private final String prettyName;

  public static OrderKind parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
