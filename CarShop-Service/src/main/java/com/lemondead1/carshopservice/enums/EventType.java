package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public enum EventType implements HasId {
  CAR_CREATED("car_created"),
  CAR_MODIFIED("car_edited"),
  CAR_DELETED("car_deleted"),
  ORDER_CREATED("purchase_order_created"),
  ORDER_MODIFIED("purchase_order_edited"),
  ORDER_DELETED("purchase_order_deleted"),
  USER_LOGGED_IN("user_logged_in"),
  USER_SIGNED_UP("user_signed_up"),
  USER_MODIFIED("user_edited"),
  USER_CREATED("user_created");

  private final String id;

  EventType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}