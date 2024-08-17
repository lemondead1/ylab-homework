package com.lemondead1.carshopservice.servlet.cars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.car.CarQueryDTO;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Set;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@WebServlet(value = "/cars/search", asyncSupported = true)
@RequiredArgsConstructor
public class CarSearchServlet extends HttpServlet {
  private final CarService carService;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var query = objectMapper.readValue(req.getInputStream(), CarQueryDTO.class);

    var result = carService.lookupCars(
        coalesce(query.brand(), ""),
        coalesce(query.model(), ""),
        coalesce(query.productionYear(), Range.all()),
        coalesce(query.price(), Range.all()),
        coalesce(query.condition(), ""),
        query.availability() == null ? Set.of(true, false) : Set.of(query.availability()),
        coalesce(query.sorting(), CarSorting.NAME_ASC)
    );

    var resultDto = mapStruct.carListToDtoList(result);
    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getOutputStream(), resultDto);
  }
}
