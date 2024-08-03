package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.CommandAcceptor;
import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.controller.*;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.*;
import lombok.Setter;

public class CarShopServiceApplication {
  @Setter
  private static boolean exited = false;

  public static void main(String[] args) {
    var userRepo = new UserRepo();
    var carRepo = new CarRepo();
    var orderRepo = new OrderRepo();
    var eventRepo = new EventRepo();

    userRepo.setOrders(orderRepo);
    carRepo.setOrders(orderRepo);
    orderRepo.setCars(carRepo);
    orderRepo.setUsers(userRepo);
    eventRepo.setUsers(userRepo);

    userRepo.create("admin", "88005553535", "test@example.com", "password", UserRole.ADMIN);

    var timeService = new TimeService();
    var eventService = new EventService(eventRepo, timeService);
    var userService = new UserService(userRepo, eventService);
    var orderService = new OrderService(orderRepo, eventService, timeService);
    var carService = new CarService(carRepo, orderRepo, eventService);
    var sessionService = new SessionService(userService);

    var cli = new ConsoleIO(System.console(), System.out);
    var commandBuilder = new CommandRootBuilder();
    new LoginController(userService).registerEndpoints(commandBuilder);
    new HomeController().registerEndpoints(commandBuilder);
    new CarController(carService).registerEndpoints(commandBuilder);
    new OrderController(orderService).registerEndpoints(commandBuilder);
    new UserController(userService).registerEndpoints(commandBuilder);
    new EventController(eventService).registerEndpoints(commandBuilder);

    var rootCommand = commandBuilder.build();

    new CommandAcceptor(() -> !exited, cli, sessionService, rootCommand).acceptCommands();
  }
}