package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.database.DBManager;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.event.AfterTestMethodEvent;
import org.springframework.test.context.event.BeforeTestMethodEvent;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class DBInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    postgres.start();
    TestPropertyValues.of(Map.of("database.url", postgres.getJdbcUrl(),
                                 "database.username", postgres.getUsername(),
                                 "database.password", postgres.getPassword(),
                                 "database.liquibase.context", "test")).applyTo(applicationContext);
    applicationContext.addApplicationListener(event -> {
      if (event instanceof BeforeTestMethodEvent) {
        var dbManager = applicationContext.getBean(DBManager.class);
        dbManager.pushTransaction();
      } else if (event instanceof AfterTestMethodEvent) {
        var dbManager = applicationContext.getBean(DBManager.class);
        dbManager.popTransaction(true);
      }
    });
  }
}
