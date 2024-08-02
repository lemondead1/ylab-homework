package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CarService {
  private final CarRepo cars;
  private final OrderRepo orders;
  private final EventService events;

  public CarService(CarRepo cars, OrderRepo orders, EventService events) {
    this.cars = cars;
    this.orders = orders;
    this.events = events;
  }

  public int createCar(int user, String brand, String model, int yearOfIssue, int price, String condition) {
    int newCarId = cars.create(brand, model, yearOfIssue, price, condition);
    events.onCarCreated(user, newCarId, brand, model, yearOfIssue, price, condition);
    return newCarId;
  }

  public void editCar(int user, int carId, @Nullable String brand, @Nullable String model,
                      @Nullable Integer yearOfIssue, @Nullable Integer price, @Nullable String condition) {
    var newCar = cars.edit(carId).brand(brand).model(model).yearOfIssue(yearOfIssue).price(price).condition(condition)
                     .apply();
    events.onCarEdited(user, newCar);
  }

  public void deleteCar(int user, int carId, boolean cascade) {
    if (orders.existCarOrders(carId)) {
      if (cascade) {
        for (var order : orders.getCarOrders(carId)) {
          orders.delete(order.id());
        }
      } else {
        throw new CascadingException(orders.getCarOrders(carId).size() + " order(s) reference this car.");
      }
    }
    cars.delete(carId);
    events.onCarDeleted(user, carId);
  }

  public Car findById(int id) {
    return cars.findById(id);
  }

  public List<Car> lookupCars(@Nullable String brand, @Nullable String model, @Nullable Integer yearOfIssue,
                              @Nullable Integer price, @Nullable String condition, CarSorting sorting) {
    return cars.lookupCars(brand, model, yearOfIssue, price, condition, sorting).toList();
  }
}
