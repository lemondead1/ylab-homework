package com.lemondead1.carshopservice.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PurchaseOrderState {
  NEW("new"),
  SHIPPING("shipping"),
  DONE("done");

  private static final Map<String, PurchaseOrderState> idToEnumMap =
      Arrays.stream(values()).collect(Collectors.toMap(PurchaseOrderState::getId, Function.identity()));

  private final String id;

  PurchaseOrderState(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public static PurchaseOrderState idToEnum(String sqlName) {
    var result = idToEnumMap.get(sqlName);
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }
}
