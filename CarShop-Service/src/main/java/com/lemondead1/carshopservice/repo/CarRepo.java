package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import lombok.Builder;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CarRepo {
  @Setter
  private OrderRepo orders;

  private final Map<Integer, Car> cars = new HashMap<>();
  private int lastId;

  public int create(String brand, String model, int yearOfIssue, int price, String condition) {
    lastId++;
    cars.put(lastId, new Car(lastId, brand, model, yearOfIssue, price, condition));
    return lastId;
  }

  @Builder(builderMethodName = "", buildMethodName = "apply", builderClassName = "EditBuilder")
  private Car applyEdit(int id, String brand, String model, Integer yearOfIssue, Integer price, String condition) {
    var old = findById(id);

    brand = brand == null ? old.brand() : brand;
    model = model == null ? old.model() : model;
    yearOfIssue = yearOfIssue == null ? old.yearOfIssue() : yearOfIssue;
    price = price == null ? old.price() : price;
    condition = condition == null ? old.condition() : condition;

    Car newRow = new Car(id, brand, model, yearOfIssue, price, condition);
    cars.put(id, newRow);
    return newRow;
  }

  public EditBuilder edit(int id) {
    return new EditBuilder().id(id);
  }

  public Car delete(int carId) {
    if (orders.existCarOrders(carId)) {
      throw new ForeignKeyException("Cannot delete car " + carId + " as there are orders referencing it.");
    }
    var old = cars.remove(carId);
    if (old == null) {
      throw new RowNotFoundException();
    }
    return old;
  }

  public Car findById(int id) {
    if (!cars.containsKey(id)) {
      throw new RowNotFoundException("Car with id '" + id + "' not found.");
    }
    return cars.get(id);
  }

  public Stream<Car> listAll() {
    return cars.values().stream();
  }

  public Stream<Car> lookupCars(@Nullable String brand,
                                @Nullable String model,
                                @Nullable Integer yearOfIssue,
                                @Nullable Integer price,
                                @Nullable String condition,
                                CarSorting sorting) {
    var stream = listAll();
    if (brand != null) {
      var lowerCaseBrand = brand.toLowerCase();
      stream = stream.filter(car -> car.brand().toLowerCase().contains(lowerCaseBrand));
    }
    if (model != null) {
      var lowerCaseModel = model.toLowerCase();
      stream = stream.filter(car -> car.model().toLowerCase().contains(lowerCaseModel));
    }
    if (yearOfIssue != null) {
      stream = stream.filter(car -> car.yearOfIssue() == yearOfIssue);
    }
    if (price != null) {
      stream = stream.filter(car -> car.price() == price);
    }
    if (condition != null) {
      var lowerCaseCondition = condition.toLowerCase();
      stream = stream.filter(car -> car.condition().toLowerCase().contains(lowerCaseCondition));
    }
    stream = stream.sorted(switch (sorting) {
      case NAME_ASC -> Comparator.comparing(car -> car.brand() + " " + car.model(), String::compareToIgnoreCase);
      case NAME_DESC -> Comparator.comparing((Car car) -> car.brand() + " " + car.model(), String::compareToIgnoreCase)
                                  .reversed();
      case YEAR_OF_ISSUE_ASC -> Comparator.comparingInt(Car::yearOfIssue);
      case YEAR_OF_ISSUE_DESC -> Comparator.comparingInt(Car::yearOfIssue).reversed();
      case PRICE_ASC -> Comparator.comparingInt(Car::price);
      case PRICE_DESC -> Comparator.comparingInt(Car::price).reversed();
    });
    return stream;
  }
}
