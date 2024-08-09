package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;
  static EventRepo events;

  @Mock
  EventService eventService;
  CarService carService;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
    events = new EventRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @BeforeEach
  void beforeEach() {
    dbManager.init();
    carService = new CarService(cars, orders, eventService);
  }

  @AfterEach
  void afterEach() {
    dbManager.dropAll();
  }

  @Test
  void createCarCreatesCarAndPostsEvent() {
    var expectedCar = new Car(1, "Tesla", "Model 3", 2020, 5000000, "mint", true);

    assertThat(carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint")).isEqualTo(cars.findById(1))
                                                                                  .isEqualTo(expectedCar);

    verify(eventService).onCarCreated(4, expectedCar);
  }

  @Test
  void editCarEditsCarAndPostsEvent() {
    carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint");

    var newCar = new Car(1, "Tesla", "Model 3", 2020, 3000000, "used", true);

    assertThat(carService.editCar(5, 1, "Tesla", "Model 3", 2020, 3000000, "used")).isEqualTo(cars.findById(1))
                                                                                   .isEqualTo(newCar);
    verify(eventService).onCarEdited(5, newCar);
  }

  @Test
  void deleteCarDeletesCarAndPostsEvent() {
    carService.createCar(6, "Chevrolet", "Corvette", 1999, 10000000, "used");

    carService.deleteCar(10, 1, false);

    assertThatThrownBy(() -> cars.findById(1)).isInstanceOf(RowNotFoundException.class);
    verify(eventService).onCarDeleted(10, 1);
  }

  @Test
  void deleteCarThrowsCascadingExceptionWhenAnOrderExistsAndCascadeIsFalse() {
    users.create("alex", "8800555", "test@example.com", "password", UserRole.CLIENT);
    carService.createCar(53, "Chevrolet", "Camaro", 1999, 10000000, "used");
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> carService.deleteCar(6, 1, false)).isInstanceOf(CascadingException.class);
  }

  @Test
  void deleteCarDeletesCarAndOrdersWhenCascadeIsTrue() {
    users.create("alex", "8800555", "test@example.com", "password", UserRole.CLIENT);
    carService.createCar(53, "Chevrolet", "Camaro", 1999, 10000000, "used");
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    carService.deleteCar(6, 1, true);

    assertThatThrownBy(() -> cars.findById(1)).isInstanceOf(RowNotFoundException.class);
    assertThatThrownBy(() -> orders.findById(1)).isInstanceOf(RowNotFoundException.class);
  }
}
