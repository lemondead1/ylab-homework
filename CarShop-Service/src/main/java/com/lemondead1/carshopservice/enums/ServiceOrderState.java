package com.lemondead1.carshopservice.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ServiceOrderState {
  NEW("new"),
  PERFORMING("performing"),
  DONE("done"),
  CANCELLED("cancelled");

  private static final Map<String, ServiceOrderState> idToEnumMap =
      Arrays.stream(values()).collect(Collectors.toMap(ServiceOrderState::getId, Function.identity()));

  private final String id;

  ServiceOrderState(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public static ServiceOrderState idToEnum(String sqlName) {
    var result = idToEnumMap.get(sqlName);
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }
}
