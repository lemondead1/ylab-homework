package com.lemondead1.carshopservice.servlet.cars;

import com.lemondead1.carshopservice.dto.car.NewCarDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Map;

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static com.lemondead1.carshopservice.SharedTestObjects.mapStruct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CarCreationServletTest extends ServletTest {
  @Mock
  CarService carService;

  CarCreationServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new CarCreationServlet(carService, jackson, mapStruct);
  }

  @Test
  void doPostCallsCreateCarAndWritesResult() throws IOException {
    var car = new Car(400, "Mazda", "MX-30", 2020, 4000000, "new", true);
    var newCar = new NewCarDTO(car.getBrand(), car.getModel(), car.getProductionYear(), car.getPrice(), car.getCondition());

    var requestBody = jackson.writeValueAsString(newCar);

    when(carService.createCar(car.getBrand(), car.getModel(), car.getProductionYear(), car.getPrice(), car.getCondition())).thenReturn(car);
    mockReqResp(null, true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(response).setStatus(HttpStatus.CREATED_201);
    verify(response).setContentType("application/json");
    verify(carService).createCar(car.getBrand(), car.getModel(), car.getProductionYear(), car.getPrice(), car.getCondition());
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.carToCarDto(car)));
  }
}
