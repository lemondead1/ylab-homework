package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.DBInitializer;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ContextConfiguration(initializers = DBInitializer.class)
public class CarServiceTest {
  @Autowired
  CarRepo cars;

  @Autowired
  OrderRepo orders;

  @Autowired
  CarService carService;

  @BeforeEach
  void beforeEach() {
    var currentRequest = new MockHttpServletRequest();
    currentRequest.setUserPrincipal(new User(1, "admin", "88005553535", "admin@ya.com", "password", UserRole.ADMIN, 0));
    RequestContextHolder.setRequestAttributes(new ServletWebRequest(currentRequest));
  }

  @Test
  @DisplayName("createCar creates a car in the repo.")
  void createCarCreatesCar() {
    var createdCar = carService.createCar("Tesla", "Model 3", 2020, 5000000, "mint");

    assertThat(createdCar)
        .isEqualTo(cars.findById(createdCar.id()))
        .isEqualTo(new Car(createdCar.id(), "Tesla", "Model 3", 2020, 5000000, "mint", true));
  }

  @Test
  @DisplayName("editCar edits the car in the repo.")
  void editCarEditsCar() {
    var editedCar = carService.editCar(35, null, null, 2021, 454636, "good");

    assertThat(editedCar)
        .isEqualTo(cars.findById(35))
        .matches(c -> c.productionYear() == 2021 && c.price() == 454636 && "good".equals(c.condition()));
  }

  @Test
  @DisplayName("deleteCar deletes the car from the repo.")
  void deleteCarDeletesCar() {
    carService.deleteCar(99);

    assertThatThrownBy(() -> cars.findById(99)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("deleteCar throws CascadingException when there exist orders that reference this car.")
  void deleteCarThrowsCascadingExceptionWhenAnOrderExists() {
    assertThatThrownBy(() -> carService.deleteCar(1)).isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("deleteCarCascading deletes the car and related orders from the repos.")
  void deleteCarCascadingDeletesCar() {
    carService.deleteCarCascading(42);

    assertThatThrownBy(() -> cars.findById(42)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(94)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(273)).isInstanceOf(NotFoundException.class);
  }
}
