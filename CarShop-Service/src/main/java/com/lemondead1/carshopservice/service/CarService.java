package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface CarService {
  Car createCar(String brand, String model, int productionYear, int price, String condition);

  Car editCar(int carId,
              @Nullable String brand,
              @Nullable String model,
              @Nullable Integer productionYear,
              @Nullable Integer price,
              @Nullable String condition);

  /**
   * Deletes the car by id.
   *
   * @throws CascadingException if there exists an order referencing this car
   */
  void deleteCar(int carId);

  /**
   * Deletes the car and related orders.
   */
  void deleteCarCascading(int carId);

  Car findById(int carId);

  List<Car> lookupCars(String brand,
                       String model,
                       Range<Integer> productionYear,
                       Range<Integer> price,
                       String condition,
                       Collection<Boolean> availability,
                       CarSorting sorting);
}
