package com.lemondead1.carshopservice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.aspect.TransactionalAspect;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.filter.ExceptionTranslatorFilter;
import com.lemondead1.carshopservice.filter.RequestCaptorFilter;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.*;
import com.lemondead1.carshopservice.servlet.SignupServlet;
import com.lemondead1.carshopservice.servlet.cars.CarCreationServlet;
import com.lemondead1.carshopservice.servlet.cars.CarSearchServlet;
import com.lemondead1.carshopservice.servlet.cars.CarsByIdServlet;
import com.lemondead1.carshopservice.servlet.events.EventSearchServlet;
import com.lemondead1.carshopservice.servlet.orders.OrderCreationServlet;
import com.lemondead1.carshopservice.servlet.orders.OrderSearchServlet;
import com.lemondead1.carshopservice.servlet.orders.OrdersByIdServlet;
import com.lemondead1.carshopservice.servlet.users.UserCreationServlet;
import com.lemondead1.carshopservice.servlet.users.UserSearchServlet;
import com.lemondead1.carshopservice.servlet.users.UsersByIdServlet;
import com.lemondead1.carshopservice.util.HasIdModule;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Aspects;
import org.eclipse.jetty.server.Server;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class CarShopServiceApplication {
  private final ObjectMapper objectMapper;

  private final MapStruct mapStruct;

  private final DBManager dbManager;
  private final UserRepo userRepo;
  private final EventRepo eventRepo;
  private final OrderRepo orderRepo;
  private final CarRepo carRepo;

  private final TimeService timeService;
  private final EventService eventService;
  private final SessionService sessionService;
  private final UserService userService;
  private final CarService carService;
  private final OrderService orderService;

  private final Server jetty;

  public CarShopServiceApplication(Properties configs) {
    objectMapper = createObjectMapper();

    mapStruct = new MapStructImpl();

    dbManager = createDBManagerWithConfigs(configs);

    userRepo = new UserRepo(dbManager);
    eventRepo = new EventRepo(dbManager, objectMapper);
    orderRepo = new OrderRepo(dbManager);
    carRepo = new CarRepo(dbManager);

    timeService = new TimeService();
    eventService = new EventService(eventRepo, timeService);
    sessionService = new SessionService(userRepo, eventService);
    userService = new UserService(userRepo, orderRepo);
    carService = new CarService(carRepo, orderRepo);
    orderService = new OrderService(orderRepo, carRepo, timeService);

    Aspects.aspectOf(TransactionalAspect.class).setDbManager(dbManager);

    Aspects.aspectOf(AuditedAspect.class).setEventService(eventService);

    var jettyInitializer = new JettyInitializer(sessionService);

    registerWeb(jettyInitializer);

    var port = Integer.parseInt(configs.getProperty("port"));
    jetty = jettyInitializer.createJetty(port);
  }

  private void run() throws Exception {
    dbManager.migrateDatabase();
    jetty.start();
    jetty.join();
  }

  @VisibleForTesting
  static ObjectMapper createObjectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new HasIdModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  private void registerWeb(JettyInitializer jetty) {
    jetty.registerServlet(new SignupServlet(objectMapper, sessionService, mapStruct));
    jetty.registerServlet(new UsersByIdServlet(userService, orderService, mapStruct, objectMapper));
    jetty.registerServlet(new UserSearchServlet(userService, objectMapper, mapStruct));
    jetty.registerServlet(new UserCreationServlet(userService, mapStruct, objectMapper));
    jetty.registerServlet(new CarCreationServlet(carService, objectMapper, mapStruct));
    jetty.registerServlet(new CarsByIdServlet(carService, objectMapper, mapStruct));
    jetty.registerServlet(new CarSearchServlet(carService, objectMapper, mapStruct));
    jetty.registerServlet(new OrderCreationServlet(orderService, mapStruct, objectMapper));
    jetty.registerServlet(new OrdersByIdServlet(orderService, objectMapper, mapStruct));
    jetty.registerServlet(new OrderSearchServlet(orderService, mapStruct, objectMapper));
    jetty.registerServlet(new EventSearchServlet(eventService, objectMapper, mapStruct));

    var filter = new RequestCaptorFilter();
    jetty.registerFilter(filter, true);
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(filter::getCurrentPrincipal);

    jetty.registerFilter(new ExceptionTranslatorFilter(), true);
  }

  private DBManager createDBManagerWithConfigs(Properties cfg) {
    return new DBManager(cfg.getProperty("jdbc_url"),
                         cfg.getProperty("database_user"),
                         cfg.getProperty("database_password"),
                         cfg.getProperty("data_schema"),
                         cfg.getProperty("infra_schema"),
                         "db/changelog/changelog.yaml",
                         Integer.parseInt(cfg.getProperty("connection_pool_size")));
  }

  public static void main(String[] args) throws Exception {
    Properties configs = readConfigs();
    var app = new CarShopServiceApplication(configs);
    app.run();
  }

  private static Properties readConfigs() throws IOException {
    Properties cfg = new Properties();
    try (var reader = ClassLoader.getSystemResourceAsStream("config.properties")) {
      cfg.load(reader);
    }
    return cfg;
  }
}