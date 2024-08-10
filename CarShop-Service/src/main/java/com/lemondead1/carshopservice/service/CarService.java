package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.Availability;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.util.IntRange;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CarService {
  private final CarRepo cars;
  private final OrderRepo orders;
  private final EventService events;

  public CarService(CarRepo cars, OrderRepo orders, EventService events) {
    this.cars = cars;
    this.orders = orders;
    this.events = events;
  }

  public Car createCar(int userId, String brand, String model, int productionYear, int price, String condition) {
    Car newCar = cars.create(brand, model, productionYear, price, condition);
    events.onCarCreated(userId, newCar);
    return newCar;
  }

  public Car editCar(int userId, int carId,
                     @Nullable String brand,
                     @Nullable String model,
                     @Nullable Integer productionYear,
                     @Nullable Integer price,
                     @Nullable String condition) {
    var newCar = cars.edit(carId, brand, model, productionYear, price, condition);
    events.onCarEdited(userId, newCar);
    return newCar;
  }

  public void deleteCar(int userId, int carId) {
    if (orders.existCarOrders(carId)) {
      throw new CascadingException(orders.findCarOrders(carId).size() + " order(s) reference this car.");
    }

    cars.delete(carId);
    events.onCarDeleted(userId, carId);
  }

  public void deleteCarCascading(int userId, int carId) {
    orders.deleteCarOrders(carId);

    cars.delete(carId);
    events.onCarDeleted(userId, carId);
  }

  public Car findById(int carId) {
    return cars.findById(carId);
  }

  public List<Car> lookupCars(String brand,
                              String model,
                              IntRange productionYear,
                              IntRange price,
                              String condition,
                              Collection<Availability> availability,
                              CarSorting sorting) {
    var boolSet = availability.stream().map(a -> a == Availability.AVAILABLE).collect(Collectors.toSet());
    return cars.lookup(brand, model, productionYear, price, condition, boolSet, sorting);
  }
}
