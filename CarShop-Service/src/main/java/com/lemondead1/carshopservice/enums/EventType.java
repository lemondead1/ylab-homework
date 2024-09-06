package com.lemondead1.carshopservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum EventType implements HasId {
  CAR_CREATED("car_created"),
  CAR_MODIFIED("car_edited"),
  CAR_DELETED("car_deleted"),
  ORDER_CREATED("order_created"),
  ORDER_MODIFIED("order_edited"),
  ORDER_DELETED("order_deleted"),
  USER_CREATED("user_created"),
  USER_MODIFIED("user_edited"),
  USER_DELETED("user_deleted"),
  USER_LOGGED_IN("user_logged_in"),
  USER_SIGNED_UP("user_signed_up");

  public static final List<EventType> ALL = List.of(values());
  public static final Set<EventType> ALL_SET = Set.of(values());

  private static final Map<String, EventType> idToEnum = Util.createIdToValueMap(values());

  private final String id;

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }

  public static EventType parse(String id) {
    var value = idToEnum.get(id);
    if (value == null) {
      throw new IllegalArgumentException();
    }
    return value;
  }
}