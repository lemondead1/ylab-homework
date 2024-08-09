package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.CommandException;
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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;
  static EventRepo events;

  @Mock
  EventService eventService;

  @Mock
  TimeService time;

  OrderService orderService;

  Car car;
  User user;
  User user2;

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
    dbManager.setupDatabase();

    orderService = new OrderService(orders, eventService, time);

    car = cars.create("Chevrolet", "Camaro", 2000, 8000000, "like new");
    user = users.create("Username", "+74326735354", "test@example.com", "pwd", UserRole.CLIENT);
    user2 = users.create("Username2", "+74326735354", "test@example.com", "pwd", UserRole.CLIENT);
  }

  @AfterEach
  void afterEach() {
    dbManager.dropSchemas();
  }

  @Test
  void createPurchaseOrderCreatesOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var order = orderService.purchase(user.id(), car.id(), "None");
    assertThat(orders.findById(order.id())).isEqualTo(order);
    verify(eventService).onOrderCreated(user.id(), order);
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

    var order = orderService.orderService(1, 1, "None");

    assertThat(order).isEqualTo(orders.findById(2));

    verify(eventService).onOrderCreated(1, order);
  }

  @Test
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.orderService(1, 1, "None")).isInstanceOf(CarReservedException.class);
  }

  @Test
  void deleteOrderDeletesOrderAndPostsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var order = orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    orderService.deleteOrder(64, order.id());

    assertThatThrownBy(() -> orders.findById(1)).isInstanceOf(RowNotFoundException.class);
    verify(eventService).onOrderDeleted(64, 1);
  }

  @Test
  void deleteOrderThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now();

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.deleteOrder(64, 1)).isInstanceOf(CascadingException.class);
  }

  @Test
  void updateOrderStateThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now();

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.updateState(64, 1, OrderState.PERFORMING, ""))
        .isInstanceOf(CascadingException.class);
  }

  @Test
  void cancelOrderEditsStateToCancelledAndPostsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    orderService.cancel(6, 1);

    var expected = new Order(1, now, OrderKind.PURCHASE, OrderState.CANCELLED, users.findById(1), cars.findById(1), "");
    assertThat(orders.findById(1)).isEqualTo(expected);
    verify(eventService).onOrderEdited(6, expected);
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
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.updateState(7, 1, OrderState.PERFORMING, "New comment");

    var expected = new Order(1, now, OrderKind.PURCHASE, OrderState.PERFORMING,
                             users.findById(1), cars.findById(1), "New comment");
    assertThat(orders.findById(1)).isEqualTo(expected);
    verify(eventService).onOrderEdited(7, expected);
  }
}
