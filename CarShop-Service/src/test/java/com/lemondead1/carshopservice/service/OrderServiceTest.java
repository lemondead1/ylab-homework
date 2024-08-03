package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.event.OrderEvent;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  @Mock
  TimeService time;
  CarRepo cars;
  UserRepo users;
  OrderRepo orders;
  EventRepo events;
  EventService eventService;
  OrderService orderService;
  Car car;
  User user;
  User user2;

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
    eventService = new EventService(events, time);
    orderService = new OrderService(orders, eventService, time);

    car = cars.create("Chevrolet", "Camaro", 2000, 8000000, "like new");
    user = users.create("Username", "+74326735354", "test@example.com", "pwd", UserRole.CLIENT);
    user2 = users.create("Username2", "+74326735354", "test@example.com", "pwd", UserRole.CLIENT);
  }

  @Test
  void createPurchaseOrderCreatesOrderAndSubmitsEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orderService.purchase(1, 1, "None");
    assertThat(orders.findById(1)).isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW, user, car, "None"));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .containsExactly(new OrderEvent.Created(now, 1, 1, now, OrderKind.PURCHASE,
                                                                        OrderState.NEW, 1, 1, "None"));
  }

  @Test
  void createPurchaseOrderThrowsCarReservedExceptionWhenThereIsActiveOrder() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orderService.purchase(1, 1, "None");
    assertThatThrownBy(() -> orderService.purchase(2, 1, "None")).isInstanceOf(CarReservedException.class);
  }

  @Test
  void createServiceOrderCreatesSavesAnOrderAndSubmitsEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orderService.orderService(1, 1, "None");
    assertThat(orders.findById(2)).isEqualTo(new Order(2, now, OrderKind.SERVICE, OrderState.NEW, user, car, "None"));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .contains(new OrderEvent.Created(now, 1, 2, now, OrderKind.SERVICE,
                                                                 OrderState.NEW, 1, 1, "None"));
  }

  @Test
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.orderService(1, 1, "None")).isInstanceOf(CarReservedException.class);
  }

  @Test
  void deleteOrderDeletesOrderAndPostsEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.deleteOrder(64, 1);

    assertThatThrownBy(() -> orders.findById(1)).isInstanceOf(RowNotFoundException.class);
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .contains(new OrderEvent.Deleted(now, 64, 1));
  }

  @Test
  void cancelOrderEditsStateToCancelledAndPostsEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.cancel(6, 1);

    assertThat(orders.findById(1))
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.CANCELLED, user, car, ""));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .contains(new OrderEvent.Modified(now, 6, 1, now, OrderKind.PURCHASE,
                                                                  OrderState.CANCELLED, 1, 1, ""));
  }

  @Test
  void cancelOrderThrowsWhenDone() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(6, 1)).isInstanceOf(CommandException.class);
  }

  @Test
  void cancelOrderThrowsWhenCancelled() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(6, 1)).isInstanceOf(CommandException.class);
  }

  @Test
  void updateStateEditsOrderAndPostsEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);

    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.updateState(7, 1, OrderState.PERFORMING, "New comment");

    assertThat(orders.findById(1))
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.PERFORMING, user, car, "New comment"));

    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .contains(new OrderEvent.Modified(now, 7, 1, now, OrderKind.PURCHASE,
                                                                  OrderState.PERFORMING, 1, 1, "New comment"));
  }
}
