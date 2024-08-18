package com.lemondead1.carshopservice.servlet.cars;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Map;

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static com.lemondead1.carshopservice.SharedTestObjects.mapStruct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CarsByIdServletTest extends ServletTest {
  @Mock
  CarService carService;

  CarsByIdServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new CarsByIdServlet(carService, jackson, mapStruct);
  }

  @Test
  @DisplayName("doGet on /200 calls findById and writes the result.")
  void doGetCallsFindByIdAndWritesCar() throws IOException {
    var car = new Car(200, "Ford", "Fusion Sport", 2010, 3000000, "poor", true);

    when(carService.findById(200)).thenReturn(car);
    mockReqResp("/200", true, null, null, Map.of());

    servlet.doGet(request, response);

    verify(carService).findById(200);
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.carToCarDto(car)));
  }

  @Test
  @DisplayName("doPost on /200 calls editCar and writes the result.")
  void doPostCallsEditCarAndWritesNewCar() throws IOException {
    var car = new Car(200, "Fiat", "500", 1965, 2000000, "good", true);
    var requestBody = "{\"production_year\": 1965, \"price\": 2000000, \"condition\": \"good\"}";

    when(carService.editCar(200, null, null, 1965, 2000000, "good")).thenReturn(car);
    mockReqResp("/200", true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(carService).editCar(200, null, null, 1965, 2000000, "good");
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.carToCarDto(car)));
  }

  @Test
  @DisplayName("doDelete on /200 calls deleteCar.")
  void doDeleteCallsDeleteCar() throws IOException {
    mockReqResp("/200", false, null, null, Map.of());

    servlet.doDelete(request, response);

    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
    verify(carService).deleteCar(200);
  }

  @Test
  @DisplayName("doDelete on /200?cascade=true calls deleteCarCascading.")
  void doDeleteCallsDeleteCarCascading() throws IOException {
    mockReqResp("/200", false, null, null, Map.of("cascade", "true"));

    servlet.doDelete(request, response);

    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
    verify(carService).deleteCarCascading(200);
  }

  @Test
  @DisplayName("parseCarId on / throws.")
  void parseCarIdUnderflowTest() throws IOException {
    mockReqResp("/", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseCarId(request)).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("parseCarId on /NaN throws.")
  void parseCarIdNaNTest() throws IOException {
    mockReqResp("/Nan", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseCarId(request)).isInstanceOf(BadRequestException.class);
  }
}
