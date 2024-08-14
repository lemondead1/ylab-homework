package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.*;
import com.lemondead1.carshopservice.servlet.HelloWorld;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class CarShopServiceApplication {
  private static boolean exited = false;

  public static void main(String[] args) throws Exception {
    var configs = readConfigs();

//    var dbManager = createDBManagerWithConfigs(configs);
//    dbManager.setupDatabase();
//
//
//    var userRepo = new UserRepo(dbManager);
//    var carRepo = new CarRepo(dbManager);
//    var orderRepo = new OrderRepo(dbManager);
//    var eventRepo = new EventRepo(dbManager);
//
//    var timeService = new TimeService();
//    var eventService = new EventService(eventRepo, timeService);
//    var userService = new UserService(userRepo, orderRepo, eventService);
//    var orderService = new OrderService(orderRepo, carRepo, eventService, timeService);
//    var carService = new CarService(carRepo, orderRepo, eventService);
//    var sessionService = new SessionService(userRepo, eventService);

    var server = setupJettyWithConfigs(configs);
    server.start();
    server.join();
  }

  private static Server setupJettyWithConfigs(Properties cfg) {
    var port = Integer.parseInt(cfg.getProperty("port"));
    var server = new Server(port);
    var context = new WebAppContext();
    var resourceFactory = ResourceFactory.of(context);
    var baseResource = resourceFactory.newClassLoaderResource("webapp.xml");
    context.setBaseResource(baseResource);
    context.setContextPath("/");
    context.setParentLoaderPriority(true);


    context.addServlet(new HelloWorld(), "/hello");

    server.setHandler(context);
    return server;
  }

  private static Properties readConfigs() throws IOException {
    var cfg = new Properties();
    try (var reader = ClassLoader.getSystemResourceAsStream("config.properties")) {
      cfg.load(reader);
    }
    return cfg;
  }

  private static DBManager createDBManagerWithConfigs(Properties cfg) throws IOException {
    return new DBManager(cfg.getProperty("jdbc_url"),
                         cfg.getProperty("database_user"),
                         cfg.getProperty("database_password"),
                         cfg.getProperty("data_schema"),
                         cfg.getProperty("infra_schema"),
                         "db/changelog/changelog.yaml",
                         false);
  }
}