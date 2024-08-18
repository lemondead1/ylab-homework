package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import org.aspectj.lang.Aspects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
  private static final CarRepo cars = new CarRepo(TestDBConnector.DB_MANAGER);
  private static final OrderRepo orders = new OrderRepo(TestDBConnector.DB_MANAGER);

  @Mock
  EventService eventService;

  CarService carService;

  private final User dummyUser = new User(5, "dummy", "123456789", "dummy@example.com", "password", UserRole.ADMIN, 0);

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(() -> dummyUser);
    Aspects.aspectOf(AuditedAspect.class).setEventService(eventService);
    carService = new CarService(cars, orders);
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @Test
  @DisplayName("createCar creates a car in the repo and submits an event.")
  void createCarCreatesCarAndPostsEvent() {
    var createdCar = carService.createCar("Tesla", "Model 3", 2020, 5000000, "mint");

    assertThat(createdCar)
        .isEqualTo(cars.findById(createdCar.id()))
        .isEqualTo(new Car(createdCar.id(), "Tesla", "Model 3", 2020, 5000000, "mint", true));

    verify(eventService).postEvent(eq(5), eq(EventType.CAR_CREATED), any());
  }

  @Test
  @DisplayName("editCar edits the car in the repo and submits an event.")
  void editCarEditsCarAndPostsEvent() {
    var editedCar = carService.editCar(35, null, null, 2021, 454636, "good");

    assertThat(editedCar)
        .isEqualTo(cars.findById(35))
        .matches(c -> c.productionYear() == 2021 && c.price() == 454636 && "good".equals(c.condition()));

    verify(eventService).postEvent(eq(5), eq(EventType.CAR_MODIFIED), any());
  }

  @Test
  @DisplayName("deleteCar deletes the car from the repo and submits an event.")
  void deleteCarDeletesCarAndPostsEvent() {
    carService.deleteCar(99);

    assertThatThrownBy(() -> cars.findById(99)).isInstanceOf(NotFoundException.class);

    verify(eventService).postEvent(eq(5), eq(EventType.CAR_DELETED), any());
  }

  @Test
  @DisplayName("deleteCar throws CascadingException when there exist orders that reference this car.")
  void deleteCarThrowsCascadingExceptionWhenAnOrderExists() {
    assertThatThrownBy(() -> carService.deleteCar(1)).isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("deleteCarCascading deletes the car and related orders from the repos and submits events.")
  void deleteCarCascadingDeletesCar() {
    carService.deleteCarCascading(42);

    assertThatThrownBy(() -> cars.findById(42)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(94)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(273)).isInstanceOf(NotFoundException.class);

    verify(eventService).postEvent(eq(5), eq(EventType.CAR_DELETED), any());
  }
}
