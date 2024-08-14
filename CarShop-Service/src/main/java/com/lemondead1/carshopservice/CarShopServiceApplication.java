package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.cli.CommandAcceptor;
import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.controller.*;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.*;

import java.io.IOException;
import java.util.Properties;

public class CarShopServiceApplication {
  private static boolean exited = false;

  public static void main(String[] args) throws IOException {
    var dbManager = createDBManagerWithConfigFile();
    dbManager.setupDatabase();

    var userRepo = new UserRepo(dbManager);
    var carRepo = new CarRepo(dbManager);
    var orderRepo = new OrderRepo(dbManager);
    var eventRepo = new EventRepo(dbManager);

    var timeService = new TimeService();
    var eventService = new EventService(eventRepo, timeService);
    var userService = new UserService(userRepo, orderRepo, eventService);
    var orderService = new OrderService(orderRepo, carRepo, eventService, timeService);
    var carService = new CarService(carRepo, orderRepo, eventService);
    var sessionService = new SessionService(userRepo, eventService);

    var cli = CLI.getBestAvailable();
    var commandBuilder = new CommandRootBuilder();
    new LoginController(sessionService).registerEndpoints(commandBuilder);
    new HomeController(() -> exited = true).registerEndpoints(commandBuilder);
    new CarController(carService).registerEndpoints(commandBuilder);
    new OrderController(orderService, carService, userService).registerEndpoints(commandBuilder);
    new UserController(userService).registerEndpoints(commandBuilder);
    new EventController(eventService).registerEndpoints(commandBuilder);

    var rootCommand = commandBuilder.build();

    new CommandAcceptor(() -> !exited, cli, sessionService, rootCommand, dbManager).acceptCommands();
  }

  private static DBManager createDBManagerWithConfigFile() throws IOException {
    var cfg = new Properties();
    try (var reader = ClassLoader.getSystemResourceAsStream("config.properties")) {
      cfg.load(reader);
    }
    return new DBManager(cfg.getProperty("jdbc_url"),
                         cfg.getProperty("database_user"),
                         cfg.getProperty("database_password"),
                         cfg.getProperty("data_schema"),
                         cfg.getProperty("infra_schema"),
                         "db/changelog/changelog.yaml",
                         false);
  }
}