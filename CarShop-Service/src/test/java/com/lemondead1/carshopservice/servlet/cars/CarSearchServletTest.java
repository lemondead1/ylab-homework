package com.lemondead1.carshopservice.servlet.cars;

import com.lemondead1.carshopservice.dto.car.CarQueryDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lemondead1.carshopservice.ObjectMapperHolder.jackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CarSearchServletTest extends ServletTest {
  @Mock
  CarService carService;

  CarSearchServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new CarSearchServlet(carService, jackson, mapStruct);
  }

  @Test
  @DisplayName("doPost calls lookupCars and writes the result.")
  void doPostCallsLookup() throws IOException {
    var query = new CarQueryDTO("Ferr", "", null, null, null, true, CarSorting.PRICE_ASC);
    var result = List.of(new Car(163, "Ferrari", "F355", 1997, 5000000, "good", true),
                         new Car(42, "Ferrari", "488 GTB Challenge", 2017, 10000000, "mint", true));

    var requestBody = jackson.writeValueAsString(query);

    when(carService.lookupCars("Ferr", "", Range.all(), Range.all(), "", Set.of(true), CarSorting.PRICE_ASC)).thenReturn(result);
    mockReqResp(null, true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(carService).lookupCars("Ferr", "", Range.all(), Range.all(), "", Set.of(true), CarSorting.PRICE_ASC);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.carListToDtoList(result)));
  }
}
