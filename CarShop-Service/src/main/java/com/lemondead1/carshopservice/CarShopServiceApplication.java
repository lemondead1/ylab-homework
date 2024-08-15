package com.lemondead1.carshopservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.TimeService;
import com.lemondead1.carshopservice.servlet.HelloWorldServlet;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class CarShopServiceApplication {
  private final ObjectMapper objectMapper;

  private final MapStruct mapStruct;

  private final DBManager dbManager;
  private final UserRepo userRepo;
  private final EventRepo eventRepo;

  private final TimeService timeService;
  private final EventService eventService;
  private final SessionService sessionService;

  private final Server jetty;

  public CarShopServiceApplication(Properties configs) throws IOException {
    objectMapper = new ObjectMapper();

    mapStruct = new MapStructImpl();

    dbManager = createDBManagerWithConfigs(configs);
    dbManager.populateConnectionPool();
    dbManager.setupDatabase();
    userRepo = new UserRepo(dbManager);
    eventRepo = new EventRepo(dbManager);

    timeService = new TimeService();
    eventService = new EventService(objectMapper, eventRepo, timeService);
    sessionService = new SessionService(mapStruct, userRepo, eventService);

    jetty = setupJettyWithConfigs(configs);
  }

  private void run() throws Exception {
    jetty.start();
    jetty.join();
  }

  private Server setupJettyWithConfigs(Properties cfg) {
    var port = Integer.parseInt(cfg.getProperty("port"));
    var server = new Server(port);
    server.setHandler(setupWebAppContext());
    return server;
  }

  private WebAppContext setupWebAppContext() {
    var context = new WebAppContext();

    var resourceFactory = ResourceFactory.of(context);
    var baseResource = resourceFactory.newClassLoaderResource("webapp.xml");
    context.setBaseResource(baseResource);
    context.setContextPath("/");
    context.setParentLoaderPriority(true);
    context.setSecurityHandler(setupJettySecurity());

    context.addServlet(new HelloWorldServlet(objectMapper), "/hello");

    return context;
  }

  private SecurityHandler.PathMapped setupJettySecurity() {
    var security = new SecurityHandler.PathMapped();

    security.put("*", Constraint.FORBIDDEN);
    security.put("/signup", Constraint.ALLOWED);
    security.put("/hello", Constraint.from("admin"));

    security.setAuthenticator(new BasicAuthenticator());
    security.setLoginService(sessionService);

    return security;
  }

  private DBManager createDBManagerWithConfigs(Properties cfg) {
    return new DBManager(cfg.getProperty("jdbc_url"),
                         cfg.getProperty("database_user"),
                         cfg.getProperty("database_password"),
                         cfg.getProperty("data_schema"),
                         cfg.getProperty("infra_schema"),
                         "db/changelog/changelog.yaml",
                         false,
                         Integer.parseInt(cfg.getProperty("connection_pool_size")));
  }

  public static void main(String[] args) throws Exception {
    var configs = readConfigs();
    var app = new CarShopServiceApplication(configs);
    app.run();
  }

  private static Properties readConfigs() throws IOException {
    var cfg = new Properties();
    try (var reader = ClassLoader.getSystemResourceAsStream("config.properties")) {
      cfg.load(reader);
    }
    return cfg;
  }
}