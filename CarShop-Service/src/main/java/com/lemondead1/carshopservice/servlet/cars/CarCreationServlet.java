package com.lemondead1.carshopservice.servlet.cars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.car.NewCarDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import com.lemondead1.carshopservice.validation.PastYearValidator;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.validation.Validated.validate;

@WebServlet(value = "/cars", asyncSupported = true)
@ServletSecurity(@HttpConstraint(rolesAllowed = { "manager", "admin" }))
@RequiredArgsConstructor
public class CarCreationServlet extends HttpServlet {
  private final CarService carService;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    NewCarDTO newCarDto = objectMapper.readValue(req.getReader(), NewCarDTO.class);

    Car createdCar = carService.createCar(
        validate(newCarDto.brand()).nonnull("Brand is required."),
        validate(newCarDto.model()).nonnull("Model is required."),
        validate(newCarDto.productionYear()).by(PastYearValidator.INSTANCE).nonnull("Production year is required."),
        validate(newCarDto.price()).by(Util.POSITIVE_INT).nonnull("Price is required."),
        validate(newCarDto.condition()).nonnull("Condition is required.")
    );

    resp.setContentType("application/json");
    resp.setStatus(HttpStatus.CREATED_201);
    ExistingCarDTO createdCarDto = mapStruct.carToCarDto(createdCar);
    objectMapper.writeValue(resp.getWriter(), createdCarDto);
  }
}
