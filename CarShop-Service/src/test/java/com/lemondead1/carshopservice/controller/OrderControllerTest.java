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
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> order.byId(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders);
  }

  @Test
  void byIdSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.byId(dummyUser, cli, "10")).isEqualTo("Found " + dummyOrder.prettyFormat());

    cli.assertMatchesHistory();
    verify(orders).findById(10);
  }

  @Test
  void orderPurchaseFailsWithNoParameters() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> order.purchase(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(orders);
  }

  @Test
  void orderPurchaseCancelled() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Comments > ").in("")
       .out("Ordering " + mockCar.prettyFormat() + ".\n")
       .out("Confirm [y/N] > ").in("n");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(order.purchase(dummyUser, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();

    verify(cars).findById(3);
    verifyNoMoreInteractions(cars);
    verifyNoInteractions(orders);
  }

  @Test
  void orderPurchaseSuccess() {
    var dummyUser = new User(50, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");


    cli.out("Comments > ").in("ASAP")
       .out("Ordering " + dummyCar.prettyFormat() + ".\n")
       .out("Confirm [y/N] > ").in("y");

    when(cars.findById(3)).thenReturn(dummyCar);
    when(orders.purchase(50, 3, "ASAP")).thenReturn(dummyOrder);

    assertThat(order.purchase(dummyUser, cli, "3")).isEqualTo("Ordered " + dummyCar.prettyFormat());

    cli.assertMatchesHistory();

    verify(cars).findById(3);
    verify(orders).purchase(50, 3, "ASAP");

    verifyNoMoreInteractions(orders, cars);
  }

  @Test
  void orderServiceThrowsWithoutArguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> order.service(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderServiceSuccess() {
    var dummyUser = new User(50, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Comments > ").in("ASAP");

    when(orders.orderService(50, 3, "ASAP")).thenReturn(dummyOrder);

    assertThat(order.service(dummyUser, cli, "3")).isEqualTo("Scheduled service for " + dummyCar.prettyFormat());

    cli.assertMatchesHistory();

    verify(orders).orderService(50, 3, "ASAP");

    verifyNoMoreInteractions(orders, cars);
  }

  @Test
  void orderCancelThrowsWithNoArguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> order.cancel(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderCancelThrowsWhenCancellingAnotherUserOrder() {
    var dummyUser = new User(5, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var orderOwner = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, orderOwner, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThatThrownBy(() -> order.cancel(dummyUser, cli, "10")).isInstanceOf(CommandException.class);
  }

  @Test
  void orderCancelSuccess() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.cancel(dummyUser, cli, "10")).isEqualTo("Cancelled " + dummyOrder.prettyFormat());

    verify(orders).cancel(3, 10);
  }

  @Test
  void cancelOtherUsersOrderByManagerSuccess() {
    var managerUser = new User(2, "manager", "12346789", "mail@example.com", "pass", UserRole.MANAGER, 0);
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.cancel(managerUser, cli, "10")).isEqualTo("Cancelled " + dummyOrder.prettyFormat());

    verify(orders).cancel(2, 10);
  }

  @Test
  void orderUpdateStateThrowsWithFewerThan2Arguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.MANAGER, 0);

    assertThatThrownBy(() -> order.updateState(dummyUser, cli, "5")).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderUpdateStateThrowsOnNoStateChange() {
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.MANAGER, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThatThrownBy(() -> order.updateState(dummyUser, cli, "10", "new")).isInstanceOf(CommandException.class);
  }

  @Test
  void orderUpdateStateSuccess() {
    var dummyUser = new User(20, "username", "8457435345", "test@example.com", "password", UserRole.MANAGER, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    when(orders.findById(10)).thenReturn(dummyOrder);

    cli.out("Append comment > ").in("Nothing");

    assertThat(order.updateState(dummyUser, cli, "10", "performing")).isEqualTo("Done");

    cli.assertMatchesHistory();

    verify(orders).updateState(20, 10, OrderState.PERFORMING, "\nNothing");
  }

  @Test
  void orderCreateThrowsOnLessThan2Arguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    assertThatThrownBy(() -> order.create(dummyUser, cli, "5")).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderCreateSuccess() {
    var managerUser = new User(63, "username", "12346789", "mail@example.com", "pass", UserRole.MANAGER, 0);
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Kind > ").in("purchase")
       .out("State > ").in("performing")
       .out("Comments > ").in("Comment");

    when(orders.createOrder(63, 3, 5, OrderKind.PURCHASE, OrderState.PERFORMING, "Comment")).thenReturn(dummyOrder);
    when(users.findById(3)).thenReturn(dummyUser);
    when(cars.findById(5)).thenReturn(dummyCar);

    assertThat(order.create(managerUser, cli, "3", "5")).isEqualTo("Created " + dummyOrder.prettyFormat() + ".");

    cli.assertMatchesHistory();

    verify(orders).createOrder(63, 3, 5, OrderKind.PURCHASE, OrderState.PERFORMING, "Comment");
  }

  @Test
  void orderDeleteThrowsWithoutArguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    assertThatThrownBy(() -> order.deleteOrder(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(orders, cars, users);
  }

  @Test
  void orderDeleteCancelled() {
    var dummyManager = new User(33, "username", "12346789", "mail@example.com", "pass", UserRole.MANAGER, 0);
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Deleting " + dummyOrder.prettyFormat() + "\n")
       .out("Confirm [y/N] > ").in("no");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.deleteOrder(dummyManager, cli, "10")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();

    verify(orders, times(0)).deleteOrder(33, 10);
  }

  @Test
  void orderDeleteSuccess() {
    var dummyManager = new User(33, "username", "12346789", "mail@example.com", "pass", UserRole.MANAGER, 0);
    var dummyUser = new User(3, "username", "8457435345", "test@example.com", "password", UserRole.CLIENT, 0);
    var dummyCar = new Car(5, "Brand", "Model", 2001, 1000000, "poor", false);
    var dummyOrder = new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.NEW, dummyUser, dummyCar, "");

    cli.out("Deleting " + dummyOrder.prettyFormat() + "\n")
       .out("Confirm [y/N] > ").in("yes");

    when(orders.findById(10)).thenReturn(dummyOrder);

    assertThat(order.deleteOrder(dummyManager, cli, "10")).isEqualTo("Deleted");

    cli.assertMatchesHistory();

    verify(orders).deleteOrder(33, 10);
  }
}
