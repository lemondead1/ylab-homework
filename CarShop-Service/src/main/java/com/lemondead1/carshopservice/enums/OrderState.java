package com.lemondead1.carshopservice.enums;

public enum OrderState {
  NEW("new", "New"),
  PERFORMING("performing", "Performing"),
  DONE("done", "Done"),
  CANCELLED("cancelled", "Cancelled");

  private final String id;
  private final String prettyName;

  OrderState(String id, String prettyName) {
    this.id = id;
    this.prettyName = prettyName;
  }

  public String getId() {
    return id;
  }

  public String getPrettyName() {
    return prettyName;
  }
}
