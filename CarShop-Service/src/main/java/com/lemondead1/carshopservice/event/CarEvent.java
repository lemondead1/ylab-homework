package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.JsonUtil;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public abstract class CarEvent extends Event {
  private final int carId;

  CarEvent(Instant timestamp, int userId, int carId) {
    super(timestamp, userId);
    this.carId = carId;
  }

  @Getter
  public static class Created extends CarEvent {
    private final String brand;
    private final String model;
    private final int productionYear;
    private final int price;
    private final String condition;

    public Created(Instant timestamp, int userId, int carId, String brand, String model, int productionYear, int price,
                   String condition) {
      super(timestamp, userId, carId);
      this.brand = brand;
      this.model = model;
      this.productionYear = productionYear;
      this.price = price;
      this.condition = condition;
    }

    @Override
    public EventType getType() {
      return EventType.CAR_CREATED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "car_id": %d, "brand": "%s", "model": "%s", "production_year": %d, "price": %d, "condition": "%s"}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCarId(),
                           JsonUtil.escapeCharacters(getBrand()), JsonUtil.escapeCharacters(getModel()),
                           getProductionYear(), getPrice(), JsonUtil.escapeCharacters(getCondition()));
    }
  }

  @Getter
  public static class Modified extends CarEvent {
    private final String newBrand;
    private final String newModel;
    private final int newProductionYear;
    private final int newPrice;
    private final String newCondition;

    public Modified(Instant timestamp, int userId, int carId, String newBrand, String newModel, int newProductionYear,
                    int newPrice, String newCondition) {
      super(timestamp, userId, carId);
      Objects.requireNonNull(newBrand);
      Objects.requireNonNull(newModel);
      Objects.requireNonNull(newCondition);
      this.newBrand = newBrand;
      this.newModel = newModel;
      this.newProductionYear = newProductionYear;
      this.newPrice = newPrice;
      this.newCondition = newCondition;
    }

    @Override
    public EventType getType() {
      return EventType.CAR_MODIFIED;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "car_id": %d, "new_brand": "%s", "new_model": "%s", "new_production_year": %d, "new_price": %d, "new_condition": "%s"}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCarId(),
                           JsonUtil.escapeCharacters(getNewBrand()), JsonUtil.escapeCharacters(getNewModel()),
                           getNewProductionYear(), getNewPrice(), JsonUtil.escapeCharacters(getNewCondition()));
    }
  }

  public static class Deleted extends CarEvent {
    public Deleted(Instant timestamp, int userId, int carId) {
      super(timestamp, userId, carId);
    }

    @Override
    public EventType getType() {
      return EventType.CAR_DELETED;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "car_id": %d}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCarId());
    }
  }
}
