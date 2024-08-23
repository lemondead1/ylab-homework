package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.car.CarQueryDTO;
import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.car.NewCarDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import com.lemondead1.carshopservice.validation.PastYearValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequiredArgsConstructor
public class CarController {
  private final CarService carService;
  private final MapStruct mapStruct;

  @PostMapping("/cars")
  @PreAuthorize("hasAnyAuthority('manager', 'admin')")
  ExistingCarDTO createCar(@RequestBody NewCarDTO carDTO) {
    Car createdCar = carService.createCar(
        validate(carDTO.brand()).nonnull("Brand is required."),
        validate(carDTO.model()).nonnull("Model is required."),
        validate(carDTO.productionYear()).by(PastYearValidator.INSTANCE).nonnull("Production year is required."),
        validate(carDTO.price()).by(Util.POSITIVE_INT).nonnull("Price is required."),
        validate(carDTO.condition()).nonnull("Condition is required.")
    );
    return mapStruct.carToCarDto(createdCar);
  }

  @PostMapping("/cars/{carId}")
  @PreAuthorize("isAuthenticated()")
  ExistingCarDTO findById(@PathVariable int carId) {
    return mapStruct.carToCarDto(carService.findById(carId));
  }

  @PostMapping("/cars/{carId}")
  @PreAuthorize("hasAnyAuthority('manager', 'admin')")
  ExistingCarDTO editById(@PathVariable int carId, @RequestBody NewCarDTO carDTO) {
    Car editedCar = carService.editCar(
        carId,
        carDTO.brand(),
        carDTO.model(),
        validate(carDTO.productionYear()).by(PastYearValidator.INSTANCE).orNull(),
        validate(carDTO.price()).by(Util.POSITIVE_INT).orNull(),
        carDTO.condition()
    );
    return mapStruct.carToCarDto(editedCar);
  }

  @PostMapping("/cars/{carId}")
  @PreAuthorize("hasAuthority('manager') and !#cascade or hasAuthority('admin')")
  void deleteById(@PathVariable int carId, @RequestParam(defaultValue = "false") boolean cascade) {
    if (cascade) {
      carService.deleteCarCascading(carId);
    } else {
      carService.deleteCar(carId);
    }
  }

  @PostMapping("/cars/search")
  @PreAuthorize("isAuthenticated()")
  List<ExistingCarDTO> search(@RequestBody CarQueryDTO queryDTO) {
    List<Car> foundCars = carService.lookupCars(
        coalesce(queryDTO.brand(), ""),
        coalesce(queryDTO.model(), ""),
        coalesce(queryDTO.productionYear(), Range.all()),
        coalesce(queryDTO.price(), Range.all()),
        coalesce(queryDTO.condition(), ""),
        Optional.ofNullable(queryDTO.availability()).map(Set::of).orElse(Set.of(true, false)),
        coalesce(queryDTO.sorting(), CarSorting.NAME_ASC)
    );
    return mapStruct.carListToDtoList(foundCars);
  }
}
