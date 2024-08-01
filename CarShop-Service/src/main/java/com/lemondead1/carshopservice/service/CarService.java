package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.repo.CarRepo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CarService {
  private final CarRepo cars;
  private final EventService events;

  public CarService(CarRepo cars, EventService events) {
    this.cars = cars;
    this.events = events;
  }

  public int createCar(int user, String brand, String model, int yearOfIssue, int price, String condition) {
    int newCarId = cars.create(brand, model, yearOfIssue, price, condition);
    events.onCarCreated(user, newCarId, brand, model, yearOfIssue, price, condition);
    return newCarId;
  }

  public void editCar(int user, int carId, String newBrand, String newModel, int newYearOfIssue, int newPrice,
                      String newCondition) {
    cars.edit(carId, newBrand, newModel, newYearOfIssue, newPrice, newCondition);
    events.onCarEdited(user, carId, newBrand, newModel, newYearOfIssue, newPrice, newCondition);
  }

  public void deleteCar(int user, int carId) {
    //TODO cleanup orders

    cars.delete(carId);
    events.onCarDeleted(user, carId);
  }

  public Car findById(int id) {
    return cars.findById(id);
  }

  public List<Car> lookupCars(@Nullable String brand, @Nullable String model, @Nullable Integer yearOfIssue,
                              @Nullable Integer price, @Nullable String condition, @Nullable CarSorting sorting) {
    return cars.lookupCars(brand, model, yearOfIssue, price, condition, sorting);
  }
}
