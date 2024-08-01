package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.CarController;
import com.lemondead1.carshopservice.cli.HomeController;
import com.lemondead1.carshopservice.cli.OrderController;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.cli.LoginController;
import com.lemondead1.carshopservice.cli.command.builders.CommandTreeBuilder;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.*;
import com.lemondead1.carshopservice.service.*;

public class CarShopServiceApplication {
  private static boolean exited = false;

  public static void setExited(boolean exited) {
    CarShopServiceApplication.exited = exited;
  }

  public static void main(String[] args) {
    LoggerService logger = new LoggerService();

    var userRepo = new UserRepo();

    userRepo.create("admin", "password", UserRole.ADMIN);

    var carRepo = new CarRepo();
    var eventRepo = new EventRepo();
    var orderRepo = new OrderRepo(logger, userRepo, carRepo);

    var timeService = new TimeService();
    var eventService = new EventService(eventRepo, timeService);
    var userService = new UserService(userRepo, eventService);
    var sessionService = new SessionService(userService);
    var carService = new CarService(carRepo,eventService);

    var cli = new ConsoleIO();
    var commandBuilder = new CommandTreeBuilder();
    new LoginController(userService).registerEndpoints(commandBuilder);
    new HomeController().registerEndpoints(commandBuilder);
    new CarController(carService).registerEndpoints(commandBuilder);
    new OrderController(orderRepo).registerEndpoints(commandBuilder);

    var rootCommand = commandBuilder.build();

    while (!exited) {
      var path = cli.readInteractive("> ");
      if (path.isEmpty()) {
        continue;
      }
      var split = path.split(" +");
      try {
        rootCommand.execute(sessionService, cli, split);
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}