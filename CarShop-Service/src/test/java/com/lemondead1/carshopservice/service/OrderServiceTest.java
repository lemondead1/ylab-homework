package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.RequestException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
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
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static CarRepo cars;
  static OrderRepo orders;

  @Mock
  EventService eventService;

  @Mock
  TimeService time;

  OrderService orderService;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(),
                              postgres.getPassword(), "data", "infra", "db/changelog/test-changelog.yaml", true);
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
    orderService = new OrderService(orders, cars, eventService, time);
  }

  @Test
  @DisplayName("purchase creates a purchase order in the repo and submits an event.")
  void createPurchaseOrderCreatesOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.purchase(53, 97, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).onOrderCreated(53, created);
  }

  @Test
  @DisplayName("purchase throws CarReservedException when the car is not available for purchase.")
  void createPurchaseOrderThrowsCarReservedExceptionWhenThereIsActiveOrder() {
    assertThatThrownBy(() -> orderService.purchase(71, 4, "None")).isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("orderService creates a service order in the repo and submits an event.")
  void createServiceOrderCreatesSavesAnOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.orderService(11, 7, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).onOrderCreated(11, created);
  }

  @Test
  @DisplayName("orderService throws CarReservedException when the car does not belong to the user.")
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.orderService(1, 1, "None")).isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("deleteOrder deletes order from the repo and submits an event.")
  void deleteOrderDeletesOrderAndPostsEvent() {
    orderService.deleteOrder(1, 232);

    assertThatThrownBy(() -> orders.findById(232)).isInstanceOf(NotFoundException.class);
    verify(eventService).onOrderDeleted(1, 232);
  }

  @Test
  @DisplayName("deleteOrder throws CascadingException when the purchase order is in 'done' state and there exist service orders for the same car and user.")
  void deleteOrderThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.deleteOrder(64, 1)).isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("updateState throws CascadingException when the purchase order is in 'done' state and there exist service orders for the same car and user.")
  void updateOrderStateThrowsOnOwnershipConstraintViolation() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    orders.create(now, OrderKind.SERVICE, OrderState.NEW, 1, 1, "");

    assertThatThrownBy(() -> orderService.updateState(64, 1, OrderState.PERFORMING, ""))
        .isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("cancel edits the order state to 'cancelled' and submits an event.")
  void cancelOrderEditsStateToCancelledAndPostsEvent() {
    orderService.cancel(218);

    var found = orders.findById(218);
    assertThat(found).matches(o -> o.state() == OrderState.CANCELLED);
    verify(eventService).onOrderEdited(6, found);
  }

  @Test
  @DisplayName("cancel throws when the order is in 'done' state.")
  void cancelOrderThrowsWhenDone() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(1)).isInstanceOf(RequestException.class);
  }

  @Test
  @DisplayName("cancel throws when order is in 'cancelled' state.")
  void cancelOrderThrowsWhenCancelled() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(1)).isInstanceOf(RequestException.class);
  }

  @Test
  @DisplayName("updateState edits the order in the repo and submits an event.")
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
