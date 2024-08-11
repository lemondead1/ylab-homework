package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static CarRepo cars;
  static OrderRepo orders;

  @Mock
  EventService eventService;
  CarService carService;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(),
                              postgres.getPassword(), "data", "infra", true);
    dbManager.setupDatabase();
    cars = new CarRepo(dbManager);
    orders = new OrderRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @BeforeEach
  void beforeEach() {
    carService = new CarService(cars, orders, eventService);
  }

  @Test
  @DisplayName("createCar creates a car in the repo and submits an event.")
  void createCarCreatesCarAndPostsEvent() {
    var createdCar = carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint");

    Car expectedCar = new Car(createdCar.id(), "Tesla", "Model 3", 2020, 5000000, "mint", true);

    assertThat(createdCar).isEqualTo(cars.findById(createdCar.id())).isEqualTo(expectedCar);

    verify(eventService).onCarCreated(4, expectedCar);
  }

  @Test
  @DisplayName("editCar edits the car in the repo and submits an event.")
  void editCarEditsCarAndPostsEvent() {
    var editedCar = carService.editCar(5, 35, null, null, 2021, 454636, "good");

    assertThat(editedCar)
        .isEqualTo(cars.findById(35))
        .matches(c -> c.productionYear() == 2021 && c.price() == 454636 && "good".equals(c.condition()));

    verify(eventService).onCarEdited(5, editedCar);
  }

  @Test
  @DisplayName("deleteCar deletes the car from the repo and submits an event.")
  void deleteCarDeletesCarAndPostsEvent() {
    carService.deleteCar(10, 99);
    assertThatThrownBy(() -> cars.findById(99)).isInstanceOf(RowNotFoundException.class);
    verify(eventService).onCarDeleted(10, 99);
  }

  @Test
  @DisplayName("deleteCar throws CascadingException when there exist orders that reference this car.")
  void deleteCarThrowsCascadingExceptionWhenAnOrderExists() {
    assertThatThrownBy(() -> carService.deleteCar(6, 1)).isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("deleteCarCascading deletes the car and related orders from the repos and submits events.")
  void deleteCarCascadingDeletesCar() {
    carService.deleteCarCascading(1, 42);

    assertThatThrownBy(() -> cars.findById(42)).isInstanceOf(RowNotFoundException.class);
    assertThatThrownBy(() -> orders.findById(94)).isInstanceOf(RowNotFoundException.class);
    assertThatThrownBy(() -> orders.findById(273)).isInstanceOf(RowNotFoundException.class);

    inOrder(eventService);
    verify(eventService).onCarDeleted(1, 42);
    verify(eventService).onOrderDeleted(1, 94);
    verify(eventService).onOrderDeleted(1, 273);
  }
}
