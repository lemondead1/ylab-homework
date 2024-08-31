package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.audit.annotations.Audited;
import com.lemondead1.logging.annotations.Timed;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Timed
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
  private final CarRepo cars;
  private final OrderRepo orders;

  @Transactional
  @Audited("car_created")
  @Override
  public Car createCar(@Audited.Param("brand") String brand,
                       @Audited.Param("model") String model,
                       @Audited.Param("production_year") int productionYear,
                       @Audited.Param("price") int price,
                       @Audited.Param("condition") String condition) {
    return cars.create(brand, model, productionYear, price, condition);
  }

  @Transactional
  @Audited("car_edited")
  @Override
  public Car editCar(@Audited.Param("car_id") int carId,
                     @Audited.Param("new_brand") @Nullable String brand,
                     @Audited.Param("new_model") @Nullable String model,
                     @Audited.Param("new_production_year") @Nullable Integer productionYear,
                     @Audited.Param("new_price") @Nullable Integer price,
                     @Audited.Param("new_condition") @Nullable String condition) {
    return cars.edit(carId, brand, model, productionYear, price, condition);
  }

  @Transactional
  @Audited("car_deleted")
  @Override
  public void deleteCar(@Audited.Param("car_id") int carId) {
    if (orders.existCarOrders(carId)) {
      throw new CascadingException(orders.findCarOrders(carId).size() + " order(s) reference this car.");
    }

    cars.delete(carId);
  }

  @Transactional
  @Audited("car_deleted")
  @Override
  public void deleteCarCascading(@Audited.Param("car_id") int carId) {
    orders.deleteCarOrders(carId);
    cars.delete(carId);
  }

  @Transactional
  @Override
  public Car findById(int carId) {
    return cars.findById(carId);
  }

  @Transactional
  @Override
  public List<Car> lookupCars(String brand,
                              String model,
                              Range<Integer> productionYear,
                              Range<Integer> price,
                              String condition,
                              Collection<Boolean> availability,
                              CarSorting sorting) {
    return cars.lookup(brand, model, productionYear, price, condition, Set.copyOf(availability), sorting);
  }
}
