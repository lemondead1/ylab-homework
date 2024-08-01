package com.lemondead1.carshopservice.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum OrderState {
  NEW("new"),
  PERFORMING("performing"),
  DONE("done"),
  CANCELLED("cancelled");

  private static final Map<String, OrderState> idToEnumMap =
      Arrays.stream(values()).collect(Collectors.toMap(OrderState::getId, Function.identity()));

  private final String id;

  OrderState(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public static OrderState idToEnum(String sqlName) {
    var result = idToEnumMap.get(sqlName);
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }
}
