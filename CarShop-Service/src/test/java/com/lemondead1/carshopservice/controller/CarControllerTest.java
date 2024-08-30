package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.car.CarQueryDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerTest {
  @MockBean
  CarService carService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  MapStruct mapStruct;

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("POST /cars calls CarService.createCar and responds with the created car.")
  void createCarTest() throws Exception {
    var c = new Car(6, "Suzuki", "Solio", 2010, 400000, "Good", true);
    var requestBody = objectMapper.writeValueAsString(mapStruct.carToNewCarDto(c));
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.carToCarDto(c));

    when(carService.createCar(c.brand(), c.model(), c.productionYear(), c.price(), c.condition())).thenReturn(c);

    mockMvc.perform(post("/cars").content(requestBody).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isCreated())
           .andExpect(content().string(expectedResponse));

    verify(carService).createCar(c.brand(), c.model(), c.productionYear(), c.price(), c.condition());
  }

  @Test
  @DisplayName("GET /cars/6 calls CarService.findById(6) and responds with the result.")
  void findCarByIdTest() throws Exception {
    var c = new Car(6, "Suzuki", "Solio", 2010, 400000, "Good", true);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.carToCarDto(c));

    when(carService.findById(6)).thenReturn(c);

    mockMvc.perform(get("/cars/6").contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(carService).findById(6);
  }

  @Test
  @DisplayName("PATCH /cars/6 calls CarService.editCar and responds with the edited car.")
  void editCarByIdTest() throws Exception {
    var newCar = new Car(6, "Suzuki", "Solio", 2011, 500000, "Mint", true);
    var requestBody = "{\"production_year\": 2011, \"price\": 500000, \"condition\": \"Mint\"}";
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.carToCarDto(newCar));

    when(carService.editCar(6, null, null, 2011, 500000, "Mint")).thenReturn(newCar);

    mockMvc.perform(patch("/cars/6").content(requestBody).contentType("application/json")
                                    .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(carService).editCar(6, null, null, 2011, 500000, "Mint");
  }

  @ParameterizedTest(name = "DELETE /cars/{0}?cascade={1} calls CarService.deleteCar{2}.")
  @CsvSource({ "53, false, ''", "75, true, Cascading" })
  @DisplayName("Testing deleteCarById.")
  void deleteCarById(int id, boolean cascade) throws Exception {
    var principal = new User(1, "user", "88005553535", "user@example.com", "password", UserRole.ADMIN, 0);

    mockMvc.perform(delete("/cars/{0}?cascade={1}", id, cascade).principal(principal)
                                                                .contentType("application/json")
                                                                .accept("application/json"))
           .andDo(log())
           .andExpect(status().isNoContent());

    if (cascade) {
      verify(carService).deleteCarCascading(id);
    } else {
      verify(carService).deleteCar(id);
    }
  }

  @Test
  @DisplayName("DELETE /cars/7 responds with FORBIDDEN when cascading and the principal is not an admin.")
  void deleteCarByIdThrowsForbiddenWhenCascadingAndDoneByManager() throws Exception {
    var principal = new User(1, "user", "88005553535", "user@example.com", "password", UserRole.MANAGER, 0);

    mockMvc.perform(delete("/cars/7?cascade=true").principal(principal)
                                                  .contentType("application/json")
                                                  .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verifyNoInteractions(carService);
  }

  @Test
  @DisplayName("POST /cars/search calls carService.lookupCars and responds with the result.")
  void searchCarsTest() throws Exception {
    var queryDTO = new CarQueryDTO("to", "co", new Range<>(2000, 2010), null, null, null, CarSorting.NAME_ASC);
    var result = List.of(new Car(64, "Toyota", "Corolla", 2007, 300000, "Good", false));
    var requestBody = objectMapper.writeValueAsString(queryDTO);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.carListToDtoList(result));

    when(carService.lookupCars("to",
                               "co",
                               new Range<>(2000, 2010),
                               Range.all(),
                               "",
                               Set.of(true, false),
                               CarSorting.NAME_ASC)).thenReturn(result);

    mockMvc.perform(post("/cars/search").content(requestBody).contentType("application/json")
                                        .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(carService).lookupCars("to",
                                  "co",
                                  new Range<>(2000, 2010),
                                  Range.all(),
                                  "",
                                  Set.of(true, false),
                                  CarSorting.NAME_ASC);
  }
}
