package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ControllerTest {
  @Mock
  CarService cars;

  @Mock
  EventService events;

  @Mock
  OrderService orders;

  @Mock
  UserService users;

  @Mock
  SessionService session;

  @Mock
  Runnable exitRunnable;

  MockCLI cli;

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
    login = new LoginController(users, session);
    order = new OrderController(orders, cars, users);
    user = new UserController(users);

    cli = new MockCLI();
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
}
