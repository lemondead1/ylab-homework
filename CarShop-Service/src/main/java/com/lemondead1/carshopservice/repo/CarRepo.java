package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CarRepo {
  private final Map<Integer, Car> cars = new HashMap<>();
  private int lastId;

  public int create(String brand, String model, int yearOfIssue, int price, String condition) {
    lastId++;
    cars.put(lastId, new Car(lastId, brand, model, yearOfIssue, price, condition));
    return lastId;
  }

  public void edit(int carId, String newBrand, String newModel, int newYearOfIssue, int newPrice, String newCondition) {
    if (!cars.containsKey(carId)) {
      throw new RowNotFoundException();
    }
    cars.put(carId, new Car(carId, newBrand, newModel, newYearOfIssue, newPrice, newCondition));
  }

  public Car delete(int carId) {
    var old = cars.remove(carId);
    if (old == null) {
      throw new RowNotFoundException();
    }
    return old;
  }

  public List<Car> listAllCars() {
    return new ArrayList<>(cars.values());
  }

  public Car findById(int id) {
    if (!cars.containsKey(id)) {
      throw new RowNotFoundException("Car with id '" + id + "' not found.");
    }
    return cars.get(id);
  }

  public List<Car> lookupCars(@Nullable String brand,
                              @Nullable String model,
                              @Nullable Integer yearOfIssue,
                              @Nullable Integer price,
                              @Nullable String condition,
                              @Nullable CarSorting sorting) {
    List<Car> result = new ArrayList<>();
    for (var car : cars.values()) {
      if ((brand == null || car.brand().contains(brand)) &&
          (model == null || car.model().contains(model)) &&
          (yearOfIssue == null || car.yearOfIssue() == yearOfIssue) &&
          (price == null || car.price() == price) &&
          (condition == null || car.condition().contains(condition))) {
        result.add(car);
      }
    }
    if (sorting != null) {
      Comparator<Car> sorter = switch (sorting) {
        case NAME_ASC -> Comparator.comparing(car -> car.brand().toLowerCase() + " " + car.model().toLowerCase());
        case NAME_DESC ->
            Comparator.comparing((Car car) -> car.brand().toLowerCase() + " " + car.model().toLowerCase()).reversed();
        case YEAR_OF_ISSUE_ASC -> Comparator.comparingInt(Car::yearOfIssue);
        case YEAR_OF_ISSUE_DESC -> Comparator.comparingInt(Car::yearOfIssue).reversed();
        case PRICE_ASC -> Comparator.comparingInt(Car::price);
        case PRICE_DESC -> Comparator.comparingInt(Car::price).reversed();
      };
      result.sort(sorter);
    }
    return result;
  }
}
