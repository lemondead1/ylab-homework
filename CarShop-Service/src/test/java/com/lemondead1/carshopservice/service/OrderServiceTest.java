package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.ConflictException;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  private static final OrderRepo orders = new OrderRepo(TestDBConnector.DB_MANAGER);
  private static final CarRepo cars = new CarRepo(TestDBConnector.DB_MANAGER);

  @Mock
  EventService eventService;

  @Mock
  TimeService time;

  OrderService orderService;

  private final User dummyUser = new User(5, "dummy", "123456789", "dummy@example.com", "password", UserRole.ADMIN, 0);

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(() -> dummyUser);
    Aspects.aspectOf(AuditedAspect.class).setEventService(eventService);
    orderService = new OrderService(orders, cars, time);
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @Test
  @DisplayName("purchase creates a purchase order in the repo and submits an event.")
  void createPurchaseOrderCreatesOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.createOrder(53, 97, OrderKind.PURCHASE, OrderState.NEW, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).postEvent(eq(5), eq(EventType.ORDER_CREATED), any());
  }

  @Test
  @DisplayName("purchase throws ForbiddenException when the car is not available for purchase.")
  void createPurchaseOrderThrowsCarReservedExceptionWhenThereIsActiveOrder() {
    assertThatThrownBy(() -> orderService.createOrder(71, 4, OrderKind.PURCHASE, OrderState.NEW, "None"))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("orderService creates a service order in the repo and submits an event.")
  void createServiceOrderCreatesSavesAnOrderAndSubmitsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    when(time.now()).thenReturn(now);

    var created = orderService.createOrder(11, 7, OrderKind.SERVICE, OrderState.NEW, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
    verify(eventService).postEvent(eq(5), eq(EventType.ORDER_CREATED), any());
  }

  @Test
  @DisplayName("orderService throws ForbiddenException when the car does not belong to the user.")
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.createOrder(1, 1, OrderKind.SERVICE, OrderState.NEW, "None"))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("deleteOrder deletes order from the repo and submits an event.")
  void deleteOrderDeletesOrderAndPostsEvent() {
    orderService.deleteOrder(232);

    assertThatThrownBy(() -> orders.findById(232)).isInstanceOf(NotFoundException.class);
    verify(eventService).postEvent(eq(5), eq(EventType.ORDER_DELETED), any());
  }

  @Test
  @DisplayName("deleteOrder throws CascadingException when the purchase order is in 'done' state and there exist service orders for the same car and user.")
  void deleteOrderThrowsOnOwnershipConstraintViolation() {
    assertThatThrownBy(() -> orderService.deleteOrder(52)).isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("updateState throws CascadingException when the purchase order is in 'done' state and there exist service orders for the same car and user.")
  void updateOrderStateThrowsOnOwnershipConstraintViolation() {
    assertThatThrownBy(() -> orderService.updateState(52, OrderState.PERFORMING, ""))
        .isInstanceOf(CascadingException.class);
  }

  @Test
  @DisplayName("cancel edits the order state to 'cancelled' and submits an event.")
  void cancelOrderEditsStateToCancelledAndPostsEvent() {
    orderService.cancel(218, "\nAppend");

    var found = orders.findById(218);
    assertThat(found).matches(o -> o.state() == OrderState.CANCELLED)
                     .matches(o -> o.comments().equals("QXArKUVHMEXsDupqDxgQ\nAppend"));
    verify(eventService).postEvent(eq(5), eq(EventType.ORDER_MODIFIED), any());
  }

  @Test
  @DisplayName("cancel throws when the order is in 'done' state.")
  void cancelOrderThrowsWhenDone() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.DONE, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(1, "")).isInstanceOf(ConflictException.class);
  }

  @Test
  @DisplayName("cancel throws when order is in 'cancelled' state.")
  void cancelOrderThrowsWhenCancelled() {
    var created = orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, 1, 1, "");
    assertThatThrownBy(() -> orderService.cancel(created.id(), "")).isInstanceOf(ConflictException.class);
  }

  @Test
  @DisplayName("updateState edits the order in the repo and submits an event.")
  void updateStateEditsOrderAndPostsEvent() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.updateState(153, OrderState.PERFORMING, "New comment");

    var found = orders.findById(153);
    assertThat(found).matches(o -> o.state() == OrderState.PERFORMING)
                     .matches(o -> "fuCupMgTVufDxoGErKGONew comment".equals(o.comments()));
    verify(eventService).postEvent(eq(5), eq(EventType.ORDER_MODIFIED), any());
  }
}
