package com.lemondead1.carshopservice.servlet.cars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.cli.validation.PastYearValidator;
import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.car.NewCarDTO;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.cli.validation.Validated.validate;

@WebServlet(value = "/cars/*", asyncSupported = true)
@ServletSecurity(httpMethodConstraints = {
    @HttpMethodConstraint(value = "GET", rolesAllowed = { "client", "manager", "admin" }),
    @HttpMethodConstraint(value = "DELETE", rolesAllowed = { "manager", "admin" }),
    @HttpMethodConstraint(value = "POST", rolesAllowed = { "manager", "admin" })
})
@RequiredArgsConstructor
public class CarsByIdServlet extends HttpServlet {
  private final CarService carService;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int id = parseCarId(req);
    var car = carService.findById(id);
    var carDto = mapStruct.carToCarDto(car);
    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getOutputStream(), carDto);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int id = parseCarId(req);
    var receivedCarDto = objectMapper.readValue(req.getInputStream(), NewCarDTO.class);
    var newCar = carService.editCar(
        id,
        receivedCarDto.brand(),
        receivedCarDto.model(),
        validate(receivedCarDto.productionYear()).by(PastYearValidator.INSTANCE).orNull(),
        validate(receivedCarDto.price()).by(Util.POSITIVE_INT).orNull(),
        receivedCarDto.condition()
    );

    var newCarDto = mapStruct.carToCarDto(newCar);
    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getOutputStream(), newCarDto);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    int id = parseCarId(req);
    var cascade = "true".equals(req.getParameter("cascade"));
    if (cascade) {
      carService.deleteCar(id);
    } else {
      carService.deleteCarCascading(id);
    }
    resp.setStatus(HttpStatus.NO_CONTENT_204);
  }

  private int parseCarId(HttpServletRequest req) {
    var split = req.getPathInfo().split("/");
    if (split.length < 2) {
      throw new BadRequestException("Car id must be specified.");
    }
    try {
      return Integer.parseInt(split[1]);
    } catch (NumberFormatException e) {
      throw new BadRequestException(Util.format("'{}' is not an integer", split[1]));
    }
  }
}
