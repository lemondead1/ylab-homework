package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.car.CarQueryDTO;
import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.car.NewCarDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import com.lemondead1.carshopservice.validation.PastYearValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequestMapping(value = "/cars", consumes = "application/json", produces = "application/json")
@RequiredArgsConstructor
public class CarController {
  private final CarService carService;
  private final MapStruct mapStruct;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a car.", description = "Creates a car. Not allowed for clients.")
  @ApiResponse(responseCode = "201", description = "Created a new car successfully.")
  ExistingCarDTO createCar(@RequestBody NewCarDTO carDTO) {
    Car createdCar = carService.createCar(
        validate(carDTO.brand()).by(Util.NOT_BLANK).nonnull("Brand is required."),
        validate(carDTO.model()).by(Util.NOT_BLANK).nonnull("Model is required."),
        validate(carDTO.productionYear()).by(PastYearValidator.INSTANCE).nonnull("Production year is required."),
        validate(carDTO.price()).by(Util.POSITIVE_INT).nonnull("Price is required."),
        validate(carDTO.condition()).by(Util.NOT_BLANK).nonnull("Condition is required.")
    );
    return mapStruct.carToCarDto(createdCar);
  }

  @GetMapping("/{carId}")
  @Operation(summary = "Fonds a car by id.", description = "Finds a car by id.")
  @ApiResponse(responseCode = "200", description = "Found a car successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find a car with the given id.", content = @Content)
  ExistingCarDTO findCarById(@PathVariable int carId) {
    return mapStruct.carToCarDto(carService.findById(carId));
  }

  @PatchMapping("/cars/{carId}")
  ExistingCarDTO editCarById(@PathVariable int carId, @RequestBody NewCarDTO carDTO) {
    Car editedCar = carService.editCar(
        carId,
        validate(carDTO.brand()).by(Util.NOT_BLANK).orNull(),
        validate(carDTO.model()).by(Util.NOT_BLANK).orNull(),
        validate(carDTO.productionYear()).by(PastYearValidator.INSTANCE).orNull(),
        validate(carDTO.price()).by(Util.POSITIVE_INT).orNull(),
        validate(carDTO.condition()).by(Util.NOT_BLANK).orNull()
    );
    return mapStruct.carToCarDto(editedCar);
  }

  @DeleteMapping("/{carId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deletes a car by id.", description = "Deletes a car by id. Not allowed for clients.")
  @ApiResponse(responseCode = "204", description = "Deleted a car successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find a car with the given id.", content = @Content)
  void deleteCarById(@PathVariable int carId, @RequestParam(defaultValue = "false") boolean cascade, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (cascade) {
      if (currentUser.role() != UserRole.ADMIN) {
        throw new ForbiddenException("Cascade is forbidden.");
      }
      carService.deleteCarCascading(carId);
    } else {
      carService.deleteCar(carId);
    }
  }

  @PostMapping("/search")
  @Operation(summary = "Searches for cars matching query.", description = "Searches for cars matching query.")
  @ApiResponse(responseCode = "200", description = "Search completed successfully.")
  List<ExistingCarDTO> searchCars(@RequestBody CarQueryDTO queryDTO) {
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
