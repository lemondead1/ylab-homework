package com.lemondead1.carshopservice.dto;

public record Car(int id, String brand, String model, int productionYear, int price, String condition) {
  public String getBrandModel() {
    return brand() + " " + model();
  }
}
