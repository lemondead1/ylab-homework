package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

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
    dbManager.setupDatabase();
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
    events = new EventRepo(dbManager);
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
  void createCarCreatesCarAndPostsEvent() {
    var createdCar = carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint");

    Car expectedCar = new Car(createdCar.id(), "Tesla", "Model 3", 2020, 5000000, "mint", true);

    assertThat(createdCar).isEqualTo(cars.findById(createdCar.id())).isEqualTo(expectedCar);

    verify(eventService).onCarCreated(4, expectedCar);
  }

  @Test
  void editCarEditsCarAndPostsEvent() {
    var editedCar = carService.editCar(5, 35, null, null, 2021, 454636, "good");

    assertThat(editedCar)
        .isEqualTo(cars.findById(35))
        .matches(c -> c.productionYear() == 2021 && c.price() == 454636 && "good".equals(c.condition()));

    verify(eventService).onCarEdited(5, editedCar);
  }

  @Test
  void deleteCarDeletesCarAndPostsEvent() {
    carService.deleteCar(10, 99);
    assertThatThrownBy(() -> cars.findById(99)).isInstanceOf(RowNotFoundException.class);
    verify(eventService).onCarDeleted(10, 99);
  }

  @Test
  void deleteCarThrowsCascadingExceptionWhenAnOrderExistsAndCascadeIsFalse() {
    assertThatThrownBy(() -> carService.deleteCar(6, 1)).isInstanceOf(CascadingException.class);
  }

  @Test
  void deleteCarCascadingDeletesCar() {
    carService.deleteCarCascading(1, 42);

    assertThatThrownBy(() -> cars.findById(42)).isInstanceOf(RowNotFoundException.class);
    assertThatThrownBy(() -> orders.findById(94)).isInstanceOf(RowNotFoundException.class);
    assertThatThrownBy(() -> orders.findById(273)).isInstanceOf(RowNotFoundException.class);
  }
}
