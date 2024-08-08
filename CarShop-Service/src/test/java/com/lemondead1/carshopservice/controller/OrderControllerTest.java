package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
  @Mock
  CarService cars;

  @Mock
  OrderService orders;

  @Mock
  SessionService session;

  @Mock
  UserService users;

  MockConsoleIO cli;

  OrderController order;

  @BeforeEach
  void setup() {
    order = new OrderController(orders, cars, users);

    cli = new MockConsoleIO();
  }

  @Test
  void byIdThrowsWithoutArguments() {
    assertThatThrownBy(() -> order.byId(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders);
  }

  @Test
  void byIdSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.byId(session, cli, "10")).isEqualTo("Found " + dummyOrder.prettyFormat());

    cli.assertMatchesHistory();
    verify(orders).findById(10);
  }

  @Test
  void orderPurchaseFailsWithNoParameters() {
    assertThatThrownBy(() -> order.purchase(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(orders);
  }

  @Test
  void orderPurchaseCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Comments > ").in("")
       .out("Ordering " + mockCar.prettyFormat() + ".\n")
       .out("Confirm [y/N] > ").in("n");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(order.purchase(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();

    verify(cars).findById(3);
    verifyNoMoreInteractions(cars);
    verifyNoInteractions(orders);
  }

  @Test
  void orderPurchaseSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Comments > ").in("ASAP")
       .out("Ordering " + mockCar.prettyFormat() + ".\n")
       .out("Confirm [y/N] > ").in("y");

    when(session.getCurrentUserId()).thenReturn(50);
    when(cars.findById(3)).thenReturn(mockCar);
    when(orders.purchase(50, 3, "ASAP")).thenReturn(mockCar);

    assertThat(order.purchase(session, cli, "3")).isEqualTo("Ordered " + mockCar.prettyFormat());

    cli.assertMatchesHistory();

    verify(cars).findById(3);
    verify(orders).purchase(50, 3, "ASAP");

    verifyNoMoreInteractions(orders, cars);
  }

  @Test
  void orderServiceThrowsWithoutArguments() {
    assertThatThrownBy(() -> order.service(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderServiceSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Comments > ").in("ASAP");

    when(session.getCurrentUserId()).thenReturn(50);
    when(orders.orderService(50, 3, "ASAP")).thenReturn(mockCar);

    assertThat(order.service(session, cli, "3")).isEqualTo("Scheduled service for " + mockCar.prettyFormat());

    cli.assertMatchesHistory();

    verify(orders).orderService(50, 3, "ASAP");

    verifyNoMoreInteractions(orders, cars);
  }

  @Test
  void orderCancelThrowsWithNoArguments() {
    assertThatThrownBy(() -> order.cancel(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderCancelThrowsWhenCancellingAnotherUserOrder() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");


    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(session.getCurrentUserId()).thenReturn(2);
    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThatThrownBy(() -> order.cancel(session, cli, "10")).isInstanceOf(CommandException.class);
  }

  @Test
  void orderCancelSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(session.getCurrentUserId()).thenReturn(3);
    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.cancel(session, cli, "10")).isEqualTo("Cancelled " + dummyOrder.prettyFormat());

    verify(orders).cancel(3, 10);
  }

  @Test
  void cancelOtherUsersOrderByManagerSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(session.getCurrentUserRole()).thenReturn(UserRole.MANAGER);
    when(session.getCurrentUserId()).thenReturn(2);
    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.cancel(session, cli, "10")).isEqualTo("Cancelled " + dummyOrder.prettyFormat());

    verify(orders).cancel(2, 10);
  }

  @Test
  void orderUpdateStateThrowsWithFewerThan2Arguments() {
    assertThatThrownBy(() -> order.updateState(session, cli, "5")).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderUpdateStateThrowsOnNoStateChange() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThatThrownBy(() -> order.updateState(session, cli, "10", "new")).isInstanceOf(CommandException.class);
  }

  @Test
  void orderUpdateStateSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);
    when(session.getCurrentUserId()).thenReturn(20);

    cli.out("Append comment > ").in("Nothing");

    assertThat(order.updateState(session, cli, "10", "performing")).isEqualTo("Done");

    cli.assertMatchesHistory();

    verify(orders).updateState(20, 10, OrderState.PERFORMING, "\nNothing");
  }

  @Test
  void orderCreateThrowsOnLessThan2Arguments() {
    assertThatThrownBy(() -> order.create(session, cli, "5")).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderCreateSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Kind > ").in("purchase")
       .out("State > ").in("performing")
       .out("Comments > ").in("Comment");

    when(orders.createOrder(63, 3, 5, OrderKind.PURCHASE, OrderState.PERFORMING, "Comment")).thenReturn(dummyOrder);
    when(users.findById(3)).thenReturn(dummyUser);
    when(cars.findById(5)).thenReturn(dummyCar);
    when(session.getCurrentUserId()).thenReturn(63);

    assertThat(order.create(session, cli, "3", "5")).isEqualTo("Created " + dummyOrder.prettyFormat() + ".");

    cli.assertMatchesHistory();

    verify(orders).createOrder(63, 3, 5, OrderKind.PURCHASE, OrderState.PERFORMING, "Comment");
  }

  @Test
  void orderDeleteThrowsWithoutArguments() {
    assertThatThrownBy(() -> order.deleteOrder(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderDeleteCancelled() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Deleting " + dummyOrder.prettyFormat() + "\n")
       .out("Confirm [y/N] > ").in("no");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.deleteOrder(session, cli, "10")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();

    verify(orders, times(0)).deleteOrder(33, 10);
  }

  @Test
  void orderDeleteSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor");
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Deleting " + dummyOrder.prettyFormat() + "\n")
       .out("Confirm [y/N] > ").in("yes");

    when(orders.findById(10)).thenReturn(dummyOrder);
    when(session.getCurrentUserId()).thenReturn(33);

    assertThat(order.deleteOrder(session, cli, "10")).isEqualTo("Deleted");

    cli.assertMatchesHistory();

    verify(orders).deleteOrder(33, 10);
  }
}
