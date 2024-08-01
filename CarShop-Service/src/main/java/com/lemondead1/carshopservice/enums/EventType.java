package com.lemondead1.carshopservice.enums;

public enum EventType {
  CAR_CREATED("car_created"),
  CAR_MODIFIED("car_modified"),
  CAR_DELETED("car_deleted"),
  ORDER_CREATED("purchase_order_created"),
  ORDER_MODIFIED("purchase_order_edited"),
  ORDER_DELETED("purchase_order_deleted"),
  USER_LOGGED_IN("user_logged_in"),
  USER_SIGNED_UP("user_signed_up");

  private final String id;

  EventType(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}