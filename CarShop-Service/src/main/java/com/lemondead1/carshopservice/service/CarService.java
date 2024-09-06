package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface CarService {
  /**
   * Creates a new car.
   *
   * @return Created car.
   */
  Car createCar(String brand, String model, int productionYear, int price, String condition);

  /**
   * Patches the car according to the arguments. Pass {@code null} to leave a field unchanged.
   *
   * @return Patched car.
   * @throws NotFoundException if the car with the given id could not be found.
   */
  Car editCar(int carId,
              @Nullable String brand,
              @Nullable String model,
              @Nullable Integer productionYear,
              @Nullable Integer price,
              @Nullable String condition);

  /**
   * Deletes the car by id.
   *
   * @throws CascadingException if there exists an order referencing this car.
   * @throws NotFoundException  if an order with the given id does not exist.
   */
  void deleteCar(int carId);

  /**
   * Deletes the car and related orders.
   *
   * @throws NotFoundException if an order with the given id does not exist.
   */
  void deleteCarCascading(int carId);

  /**
   * Searches for a car by id.
   *
   * @throws NotFoundException if the car with the given id could not be found.
   */
  Car findById(int carId);

  /**
   * Searches for cars matching the arguments.
   *
   * @param brand          Brand query.
   * @param model          Model query.
   * @param productionYear Production year range.
   * @param price          Price range.
   * @param condition      Condition query.
   * @param availability   Availability whitelist.
   * @param sorting        Sorting.
   * @return Cars matching the given arguments.
   */
  List<Car> lookupCars(String brand,
                       String model,
                       Range<Integer> productionYear,
                       Range<Integer> price,
                       String condition,
                       Collection<Boolean> availability,
                       CarSorting sorting);
}
