package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.Builder;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarRepo {
  @Setter
  private OrderRepo orders;

  private final Map<Integer, Car> cars = new HashMap<>();
  private int lastId;

  public Car create(String brand, String model, int productionYear, int price, String condition) {
    lastId++;
    Car newRow = new Car(lastId, brand, model, productionYear, price, condition);
    cars.put(lastId, newRow);
    return newRow;
  }

  @Builder(builderMethodName = "", buildMethodName = "apply", builderClassName = "EditBuilder")
  private Car applyEdit(int id,
                        @Nullable String brand,
                        @Nullable String model,
                        @Nullable Integer productionYear,
                        @Nullable Integer price,
                        @Nullable String condition) {
    var old = findById(id);

    brand = brand == null ? old.brand() : brand;
    model = model == null ? old.model() : model;
    productionYear = productionYear == null ? old.productionYear() : productionYear;
    price = price == null ? old.price() : price;
    condition = condition == null ? old.condition() : condition;

    Car newRow = new Car(id, brand, model, productionYear, price, condition);
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

  public List<Car> lookup(String brand,
                          String model,
                          IntRange productionYear,
                          IntRange price,
                          String condition,
                          CarSorting sorting) {
    var sorter = switch (sorting) {
      case NAME_ASC -> Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase);
      case NAME_DESC -> Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase).reversed();
      case PRODUCTION_YEAR_ASC -> Comparator.comparingInt(Car::productionYear);
      case PRODUCTION_YEAR_DESC -> Comparator.comparingInt(Car::productionYear).reversed();
      case PRICE_ASC -> Comparator.comparingInt(Car::price);
      case PRICE_DESC -> Comparator.comparingInt(Car::price).reversed();
    };
    return cars.values()
               .stream()
               .filter(car -> StringUtil.containsIgnoreCase(car.brand(), brand))
               .filter(car -> StringUtil.containsIgnoreCase(car.model(), model))
               .filter(car -> productionYear.test(car.productionYear()))
               .filter(car -> price.test(car.price()))
               .filter(car -> StringUtil.containsIgnoreCase(car.condition(), condition))
               .sorted(sorter)
               .toList();
  }
}
