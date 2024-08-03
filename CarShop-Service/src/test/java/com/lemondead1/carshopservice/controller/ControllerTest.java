package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.*;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ControllerTest {
  @Mock
  CarService cars;

  @Mock
  EventService events;

  @Mock
  OrderService orders;

  @Mock
  SessionService session;

  @Mock
  UserService users;

  @Mock
  Runnable exitRunnable;

  MockConsoleIO cli;

  LoginController login;
  EventController event;
  HomeController home;
  CarController car;
  OrderController order;
  UserController user;

  @BeforeEach
  void setup() {
    event = new EventController(events);
    car = new CarController(cars);
    home = new HomeController(exitRunnable);
    login = new LoginController(users);
    order = new OrderController(orders, cars, users);
    user = new UserController(users);

    cli = new MockConsoleIO();
  }

  @Test
  void registerEndpointsSucceed() {
    var builder = new CommandRootBuilder();
    event.registerEndpoints(builder);
    car.registerEndpoints(builder);
    home.registerEndpoints(builder);
    login.registerEndpoints(builder);
    order.registerEndpoints(builder);
    user.registerEndpoints(builder);
    builder.build();
  }

  @Test
  void exitCallsExitRunnable() {
    home.exit(session, cli);

    cli.assertMatchesHistory();
    verify(exitRunnable).run();
  }

  @Test
  void loginCallsUserServiceLogin() {
    cli.out("Username > ").in("username")
       .out("Password > ").in("password");

    assertThat(login.login(session, cli)).isEqualTo("Welcome, username!");

    cli.assertMatchesHistory();
  }

  @Test
  void logoutSetsCurrentUserTo0() {
    assertThat(login.logout(session, cli)).isEqualTo("Logout");

    cli.assertMatchesHistory();
    verify(session).setCurrentUserId(0);
  }

  @Test
  void signupCallsSignUserUp() {
    cli.out("Username > ").in("username")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(true);

    assertThat(login.signUp(session, cli)).isEqualTo("Signed up successfully!");

    cli.assertMatchesHistory();
    verify(users).signUserUp("username", "88005553535", "test@example.com", "password");
  }

  @Test
  void signupPrintsNameUsed() {
    cli.out("Username > ").in("username")
       .out("Username 'username' is already taken.\n")
       .out("Username > ").in("newusername")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(false);
    when(users.checkUsernameFree("newusername")).thenReturn(true);

    assertThat(login.signUp(session, cli)).isEqualTo("Signed up successfully!");

    cli.assertMatchesHistory();
    verify(users).signUserUp("newusername", "88005553535", "test@example.com", "password");
  }

  @Test
  void carDeleteFailsWhenNoParameterIsPresent() {
    assertThatThrownBy(() -> car.deleteCar(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(users);
  }

  @Test
  void carDeleteCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
  }

  @Test
  void carDeleteCascadeCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
    verify(cars, times(0)).deleteCar(5, 3, true);
  }

  @Test
  void carDeleteSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doNothing().when(cars).deleteCar(5, 3, false);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
  }

  @Test
  void carDeleteCascadingSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);
    doNothing().when(cars).deleteCar(5, 3, true);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
    verify(cars).deleteCar(5, 3, true);
  }

  @Test
  void carEditFailsWithNoArgument() {
    assertThatThrownBy(() -> car.editCar(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(cars);
  }

  @Test
  void carEditSuccess() {
    var oldCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var newCar = new Car(3, "Brand", "Model", 2001, 2000000, "ok");

    cli.out("Brand (Brand) > ").in("")
       .out("Model (Model) > ").in("")
       .out("Production year (2001) > ").in("2001")
       .out("Price (1000000) > ").in("2000000")
       .out("Condition (poor) > ").in("ok");

    when(session.getCurrentUserId()).thenReturn(5);
    when(cars.findById(3)).thenReturn(oldCar);
    when(cars.editCar(5, 3, null, null, 2001, 2000000, "ok")).thenReturn(newCar);

    assertThat(car.editCar(session, cli, "3")).isEqualTo("Saved changes to " + newCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).findById(3);
    verify(cars).editCar(5, 3, null, null, 2001, 2000000, "ok");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void createCarSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Brand > ").in("Brand")
       .out("Model > ").in("Model")
       .out("Production year > ").in("2001")
       .out("Price > ").in("1000000")
       .out("Condition > ").in("poor");

    when(session.getCurrentUserId()).thenReturn(6);
    when(cars.createCar(6, "Brand", "Model", 2001, 1000000, "poor")).thenReturn(mockCar);

    assertThat(car.createCar(session, cli)).isEqualTo("Created " + mockCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).createCar(6, "Brand", "Model", 2001, 1000000, "poor");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void eventControllerListReturnsSerializedEvents() {
    var now = Instant.now();
    var event = new UserEvent.Deleted(now, 5, 7);
    when(events.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC)).thenReturn(List.of(event));

    cli.out("Type > ").in("")
       .out("Date > ").in("")
       .out("User > ").in("")
       .out("Sorting > ").in("newer_first");

    assertThat(this.event.list(session, cli)).isEqualTo(event.serialize() + "\nRow count: 1");

    cli.assertMatchesHistory();
    verify(events).findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC);
  }

  @Test
  void eventControllerDumpsSerializedEvents() {
    cli.out("Type > ").in("")
       .out("Date > ").in("")
       .out("User > ").in("")
       .out("Sorting > ").in("newer_first")
       .out("File > ").in("events.txt");

    assertThat(event.dump(session, cli)).startsWith("Dumped event log into ");

    cli.assertMatchesHistory();

    verify(events).dumpEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC,
                              Path.of("events.txt").toAbsolutePath());
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
       .out("Ordering " + mockCar.prettyFormat() + ".")
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
       .out("Ordering " + mockCar.prettyFormat() + ".")
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
}
