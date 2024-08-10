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
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

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
    orderService = new OrderService(orders, cars, eventService, time);
  }

  @Test
  void createPurchaseOrderCreatesOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.purchase(53, 97, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).onOrderCreated(53, created);
  }

  @Test
  void createPurchaseOrderThrowsCarReservedExceptionWhenThereIsActiveOrder() {
    assertThatThrownBy(() -> orderService.purchase(71, 4, "None")).isInstanceOf(CarReservedException.class);
  }

  @Test
  void createServiceOrderCreatesSavesAnOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.orderService(11, 7, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).onOrderCreated(11, created);
  }

  @Test
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.orderService(1, 1, "None")).isInstanceOf(CarReservedException.class);
  }

  @Test
  void deleteOrderDeletesOrderAndPostsEvent() {
    orderService.deleteOrder(1, 232);

    assertThatThrownBy(() -> orders.findById(232)).isInstanceOf(RowNotFoundException.class);
    verify(eventService).onOrderDeleted(1, 232);
  }

  @Test
  void deleteOrderThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.deleteOrder(64, 1)).isInstanceOf(CascadingException.class);
  }

  @Test
  void updateOrderStateThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.updateState(64, 1, OrderState.PERFORMING, ""))
        .isInstanceOf(CascadingException.class);
  }

  @Test
  void cancelOrderEditsStateToCancelledAndPostsEvent() {
    orderService.cancel(6, 218);

    var found = orders.findById(218);
    assertThat(found).matches(o -> o.state() == OrderState.CANCELLED);
    verify(eventService).onOrderEdited(6, found);
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
    orderService.updateState(1, 153, OrderState.PERFORMING, "New comment");

    var found = orders.findById(153);
    assertThat(found)
        .matches(o -> o.state() == OrderState.PERFORMING && "fuCupMgTVufDxoGErKGONew comment".equals(o.comments()));
    verify(eventService).onOrderEdited(1, found);
  }
}
