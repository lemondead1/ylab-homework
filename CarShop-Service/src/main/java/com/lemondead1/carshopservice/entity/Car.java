package com.lemondead1.carshopservice.entity;

public record Car(int id, String brand, String model, int productionYear, int price, String condition, boolean availableForPurchase) {
  public String getBrandModel() {
    return brand() + " " + model();
  }
}
