package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

  private final String id;

  @Override
  public String getId() {
    return id;
  }
}