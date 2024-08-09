package com.lemondead1.carshopservice.entity;

public record Car(int id, String brand, String model, int productionYear, int price, String condition, boolean availableForPurchase) {
  public String getBrandModel() {
    return brand() + " " + model();
  }

  public String prettyFormat() {
    String pattern = """
        "%s" "%s" of %d p/y priced %d in "%s" condition with id %d""";
    return String.format(pattern, brand(), model(), productionYear(), price(), condition(), id());
  }
}
