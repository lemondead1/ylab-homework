package com.lemondead1.carshopservice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.aspect.TransactionalAspect;
import com.lemondead1.carshopservice.database.DBManager;
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
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServlet;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Aspects;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

  private final WebAppContext context;

  private final Server jetty;

  public static ObjectMapper createObjectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new HasIdModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  public CarShopServiceApplication(Properties configs) {
    objectMapper = createObjectMapper();

    mapStruct = new MapStructImpl();

    dbManager = createDBManagerWithConfigs(configs);
    dbManager.migrateDatabase();

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

    context = setupWebAppContext();
    jetty = setupJettyWithConfigs(configs);

    registerWeb();
  }

  private void run() throws Exception {
    jetty.start();
    jetty.join();
  }

  private Server setupJettyWithConfigs(Properties cfg) {
    var port = Integer.parseInt(cfg.getProperty("port"));
    var server = new Server(port);
    server.setHandler(context);
    return server;
  }

  private WebAppContext setupWebAppContext() {
    var context = new WebAppContext();

    var resourceFactory = ResourceFactory.of(context);
    var webResource = resourceFactory.newClassLoaderResource("web.xml");

    context.setBaseResource(webResource);
    context.setContextPath("/");
    context.setParentLoaderPriority(true);

    context.getSecurityHandler().setAuthenticator(new BasicAuthenticator());
    context.getSecurityHandler().setLoginService(sessionService);
    context.getSecurityHandler().setRealmName("car-shop");

    return context;
  }

  private void registerWeb() {
    registerServlet(new SignupServlet(objectMapper, sessionService, mapStruct));
    registerServlet(new UsersByIdServlet(userService, orderService, mapStruct, objectMapper));
    registerServlet(new UserSearchServlet(userService, objectMapper, mapStruct));
    registerServlet(new UserCreationServlet(userService, mapStruct, objectMapper));
    registerServlet(new CarCreationServlet(carService, objectMapper, mapStruct));
    registerServlet(new CarsByIdServlet(carService, objectMapper, mapStruct));
    registerServlet(new CarSearchServlet(carService, objectMapper, mapStruct));
    registerServlet(new OrderCreationServlet(orderService, mapStruct, objectMapper));
    registerServlet(new OrdersByIdServlet(orderService, objectMapper, mapStruct));
    registerServlet(new OrderSearchServlet(orderService, mapStruct, objectMapper));
    registerServlet(new EventSearchServlet(eventService, objectMapper, mapStruct));

    var filter = new RequestCaptorFilter();
    registerFilter(filter, true);
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(filter::getCurrentPrincipal);
  }

  private void registerServlet(HttpServlet servlet) {
    //Manually loading annotations since I wanted to support servlet DI.
    var dynamic = context.getServletContext().addServlet(servlet.getClass().getName(), servlet);
    var webServlet = servlet.getClass().getAnnotation(WebServlet.class);
    Objects.requireNonNull(webServlet, "WebServlet annotation is required.");
    dynamic.addMapping(webServlet.value());
    dynamic.setAsyncSupported(webServlet.asyncSupported());
    dynamic.setLoadOnStartup(webServlet.loadOnStartup());
    dynamic.setInitParameters(Arrays.stream(webServlet.initParams())
                                    .collect(Collectors.toMap(WebInitParam::name, WebInitParam::value)));
    if (servlet.getClass().isAnnotationPresent(ServletSecurity.class)) {
      context.setServletSecurity(dynamic,
                                 new ServletSecurityElement(servlet.getClass().getAnnotation(ServletSecurity.class)));
    }
  }

  private void registerFilter(HttpFilter filter, boolean matchAfter) {
    var dynamic = context.getServletContext().addFilter(filter.getClass().getName(), filter);
    var webFilter = filter.getClass().getAnnotation(WebFilter.class);
    Objects.requireNonNull(webFilter, "WebFilter annotation is required.");
    dynamic.setInitParameters(Arrays.stream(webFilter.initParams())
                                    .collect(Collectors.toMap(WebInitParam::name, WebInitParam::value)));
    dynamic.addMappingForUrlPatterns(EnumSet.copyOf(List.of(webFilter.dispatcherTypes())),
                                     matchAfter,
                                     webFilter.value());
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

  private static CarShopServiceApplication value;

  public static void main(String[] args) throws Exception {
    var configs = readConfigs();
    var app = new CarShopServiceApplication(configs);
    value = app;
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