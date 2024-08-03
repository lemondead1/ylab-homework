package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.Availability;

public record CarWithAvailability(int id, String brand, String model, int productionYear, int price, String condition,
                                  Availability availableForPurchase) {
  public Car toCar() {
    return new Car(id(), brand(), model(), productionYear(), price(), condition());
  }
}
