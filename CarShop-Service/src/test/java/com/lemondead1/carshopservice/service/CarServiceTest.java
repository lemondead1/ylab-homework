package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.*;
import com.lemondead1.carshopservice.event.CarEvent;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarServiceTest {
  CarRepo cars;
  UserRepo users;
  OrderRepo orders;
  EventRepo events;
  EventService eventService;
  CarService carService;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo();
    events = new EventRepo();
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
    events.setUsers(users);
    eventService = new EventService(events, new TimeService());
    carService = new CarService(cars, orders, eventService);
  }

  @Test
  void createCarCreatesCarAndPostsEvent() {
    carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint");
    assertThat(cars.findById(1)).isEqualTo(new Car(1, "Tesla", "Model 3", 2020, 5000000, "mint"));
    assertThat(events.lookup(Set.of(EventType.CAR_CREATED), DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .hasSize(1).map(e -> (CarEvent.Created) e).allMatch(e -> e.getUserId() == 4 && e.getCarId() == 1);
  }

  @Test
  void editCarEditsCarAndPostsEvent() {
    carService.createCar(4, "Tesla", "Model 3", 2020, 5000000, "mint");
    var car = carService.editCar(5, 1, "Tesla", "Model 3", 2020, 3000000, "used");
    assertThat(cars.findById(1)).isEqualTo(car).isEqualTo(new Car(1, "Tesla", "Model 3", 2020, 3000000, "used"));
    assertThat(events.lookup(Set.of(EventType.CAR_MODIFIED), DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .hasSize(1).map(e -> (CarEvent.Modified) e).allMatch(e -> e.getUserId() == 5 && e.getCarId() == 1);
  }

  @Test
  void deleteCarDeletesCarAndPostsEvent() {
    carService.createCar(6, "Chevrolet", "Corvette", 1999, 10000000, "used");
    carService.deleteCar(10, 1, false);
    assertThatThrownBy(() -> cars.findById(1)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteCarThrowsCascadingExceptionWhenAnOrderExistsAndCascadeIsFalse() {
    users.create("alex", "8800555", "test@example.com", "password", UserRole.CLIENT);
    carService.createCar(53, "Chevrolet", "Camaro", 1999, 10000000, "used");
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    assertThatThrownBy(() -> carService.deleteCar(6, 1, false));
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
