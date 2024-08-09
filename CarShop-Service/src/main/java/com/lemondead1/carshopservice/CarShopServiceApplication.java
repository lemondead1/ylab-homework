package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.CommandAcceptor;
import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.controller.*;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.*;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CarShopServiceApplication {
  @Setter
  private static boolean exited = false;

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      throw new IllegalStateException("Config path expected as an argument.");
    }

    var dbManager = createDBManager(args[0]);
    dbManager.init();

    var userRepo = new UserRepo(dbManager);
    var carRepo = new CarRepo(dbManager);
    var orderRepo = new OrderRepo(dbManager);
    var eventRepo = new EventRepo(dbManager);

    if (!userRepo.existsUsername("admin")) {
      userRepo.create("admin", "88005553535", "test@example.com", "password", UserRole.ADMIN);
    }

    var timeService = new TimeService();
    var eventService = new EventService(eventRepo, timeService);
    var userService = new UserService(userRepo, eventService);
    var orderService = new OrderService(orderRepo, eventService, timeService);
    var carService = new CarService(carRepo, orderRepo, eventService);
    var sessionService = new SessionService(userService, eventService);

    var cli = new ConsoleIO(System.console());
    var commandBuilder = new CommandRootBuilder();
    new LoginController(userService, sessionService).registerEndpoints(commandBuilder);
    new HomeController(() -> exited = true).registerEndpoints(commandBuilder);
    new CarController(carService).registerEndpoints(commandBuilder);
    new OrderController(orderService, carService, userService).registerEndpoints(commandBuilder);
    new UserController(userService).registerEndpoints(commandBuilder);
    new EventController(eventService).registerEndpoints(commandBuilder);

    var rootCommand = commandBuilder.build();

    new CommandAcceptor(() -> !exited, cli, sessionService, rootCommand).acceptCommands();
  }

  private static DBManager createDBManager(String configPath) throws IOException {
    var path = Path.of(configPath);
    var cfg = new Properties();
    try (var reader = Files.newBufferedReader(path)) {
      cfg.load(reader);
    }
    return new DBManager(cfg.getProperty("jdbc_url"),
                         cfg.getProperty("database_user"),
                         cfg.getProperty("database_password"),
                         cfg.getProperty("data_schema"),
                         cfg.getProperty("infra_schema"));
  }
}