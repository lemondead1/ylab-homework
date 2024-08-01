package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.JsonUtil;

import java.time.Instant;
import java.util.Objects;

public abstract class CarEvent extends Event {
  private final int carId;

  CarEvent(Instant timestamp, int userId, int carId) {
    super(timestamp, userId);
    this.carId = carId;
  }

  public int getCarId() {
    return carId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    if (!super.equals(o)) { return false; }
    CarEvent carEvent = (CarEvent) o;
    return carId == carEvent.carId;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + carId;
    return result;
  }

  public static class Created extends CarEvent {
    private final String brand;
    private final String model;
    private final int yearOfIssue;
    private final int price;
    private final String condition;

    public Created(Instant timestamp, int userId, int carId, String brand, String model, int yearOfIssue, int price,
                   String condition) {
      super(timestamp, userId, carId);
      Objects.requireNonNull(brand);
      Objects.requireNonNull(model);
      Objects.requireNonNull(condition);
      this.brand = brand;
      this.model = model;
      this.yearOfIssue = yearOfIssue;
      this.price = price;
      this.condition = condition;
    }

    public String getBrand() {
      return brand;
    }

    public String getModel() {
      return model;
    }

    public int getYearOfIssue() {
      return yearOfIssue;
    }

    public int getPrice() {
      return price;
    }

    public String getCondition() {
      return condition;
    }

    @Override
    public EventType getType() {
      return EventType.CAR_CREATED;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }
      Created created = (Created) o;
      return yearOfIssue == created.yearOfIssue && price == created.price && brand.equals(created.brand) &&
             condition.equals(created.condition);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + brand.hashCode();
      result = 31 * result + yearOfIssue;
      result = 31 * result + price;
      result = 31 * result + condition.hashCode();
      return result;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "car_id": %d, "brand": "%s", "model": "%s", "year_of_issue": %d, "price": %d, "condition": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCarId(),
                           JsonUtil.escapeCharacters(getBrand()), JsonUtil.escapeCharacters(getModel()),
                           getYearOfIssue(), getPrice(), JsonUtil.escapeCharacters(getCondition()));
    }
  }

  public static class Modified extends CarEvent {
    private final String newBrand;
    private final String newModel;
    private final int newYearOfIssue;
    private final int newPrice;
    private final String newCondition;

    public Modified(Instant timestamp, int userId, int carId, String newBrand, String newModel, int newYearOfIssue,
                    int newPrice, String newCondition) {
      super(timestamp, userId, carId);
      Objects.requireNonNull(newBrand);
      Objects.requireNonNull(newModel);
      Objects.requireNonNull(newCondition);
      this.newBrand = newBrand;
      this.newModel = newModel;
      this.newYearOfIssue = newYearOfIssue;
      this.newPrice = newPrice;
      this.newCondition = newCondition;
    }

    public String getNewBrand() {
      return newBrand;
    }

    public String getNewModel() { return newModel; }

    public int getNewYearOfIssue() {
      return newYearOfIssue;
    }

    public int getNewPrice() {
      return newPrice;
    }

    public String getNewCondition() {
      return newCondition;
    }

    @Override
    public EventType getType() {
      return EventType.CAR_MODIFIED;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }
      Modified modified = (Modified) o;
      return newYearOfIssue == modified.newYearOfIssue && newPrice == modified.newPrice &&
             newBrand.equals(modified.newBrand) && newCondition.equals(modified.newCondition);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + newBrand.hashCode();
      result = 31 * result + newYearOfIssue;
      result = 31 * result + newPrice;
      result = 31 * result + newCondition.hashCode();
      return result;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "car_id": %d, "new_brand": "%s", "new_model": "%s", "new_year_of_issue": %d, "new_price": %d, "new_condition": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCarId(),
                           JsonUtil.escapeCharacters(getNewBrand()), JsonUtil.escapeCharacters(getNewModel()),
                           getNewYearOfIssue(), getNewPrice(), JsonUtil.escapeCharacters(getNewCondition()));
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
          {"timestamp": "%s", "type": "%s", "user_id": %d}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId());
    }
  }
}
