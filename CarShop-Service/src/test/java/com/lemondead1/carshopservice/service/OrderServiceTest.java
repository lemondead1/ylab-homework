package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.ConflictException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  private static final OrderRepo orders = new OrderRepo(TestDBConnector.DB_MANAGER);
  private static final CarRepo cars = new CarRepo(TestDBConnector.DB_MANAGER);
  private static final UserRepo users = new UserRepo(TestDBConnector.DB_MANAGER);

  OrderService orderService;

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
    orderService = new OrderServiceImpl(orders, cars, users);
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @Test
  @DisplayName("purchase creates a purchase order in the repo.")
  void createPurchaseOrderCreatesOrder() {
    var created = orderService.createOrder(53, 97, OrderKind.PURCHASE, OrderState.NEW, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
  }

  @Test
  @DisplayName("purchase throws ForbiddenException when the car is not available for purchase.")
  void createPurchaseOrderThrowsCarReservedExceptionWhenThereIsActiveOrder() {
    assertThatThrownBy(() -> orderService.createOrder(71, 4, OrderKind.PURCHASE, OrderState.NEW, "None"))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  @DisplayName("orderService creates a service order in the repo.")
  void createServiceOrderCreatesSavesAnOrder() {
    var created = orderService.createOrder(11, 7, OrderKind.SERVICE, OrderState.NEW, "None");

    assertThat(created).isEqualTo(orders.findById(created.id()));
  }

  @Test
  @DisplayName("orderService throws ForbiddenException when the car does not belong to the user.")
  void createServiceOrderThrowsCarReservedExceptionWhenNoPurchaseWasPerformed() {
    assertThatThrownBy(() -> orderService.createOrder(1, 1, OrderKind.SERVICE, OrderState.NEW, "None"))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  @DisplayName("deleteOrder deletes order from the repo.")
  void deleteOrderDeletesOrder() {
    orderService.deleteOrder(232);

    assertThatThrownBy(() -> orders.findById(232)).isInstanceOf(NotFoundException.class);
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
  @DisplayName("updateState edits the order in the repo.")
  void updateStateEditsOrder() {
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orderService.updateState(153, OrderState.PERFORMING, "New comment");

    var found = orders.findById(153);
    assertThat(found).matches(o -> o.state() == OrderState.PERFORMING)
                     .matches(o -> "fuCupMgTVufDxoGErKGO\nNew comment".equals(o.comments()));
  }
}
