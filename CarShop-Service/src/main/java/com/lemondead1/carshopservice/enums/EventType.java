package com.lemondead1.carshopservice.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum EventType {
  CAR_CREATED("car_created"),
  CAR_MODIFIED("car_modified"),
  CAR_DELETED("car_deleted"),
  PURCHASE_ORDER_CREATED("purchase_order_created"),
  PURCHASE_ORDER_MODIFIED("purchase_order_edited"),
  PURCHASE_ORDER_DELETED("purchase_order_deleted"),
  SERVICE_ORDER_CREATED("service_order_created"),
  SERVICE_ORDER_MODIFIED("service_order_edited"),
  SERVICE_ORDER_DELETED("service_order_deleted"),
  USER_LOGGED_IN("user_logged_in"),
  USER_SIGNED_UP("user_signed_up");

  private static final Map<String, EventType> idToEnumMap =
      Arrays.stream(values()).collect(Collectors.toMap(EventType::getId, Function.identity()));

  private final String id;

  EventType(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public static EventType idToEnum(String sqlName) {
    var result = idToEnumMap.get(sqlName);
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }
}