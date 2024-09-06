package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.aspect.TransactionalAspect;
import com.lemondead1.carshopservice.database.DBManager;
import org.aspectj.lang.Aspects;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDBConnector {
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static {
    postgres.start();
  }

  public static final DBManager DB_MANAGER = new DBManager(postgres.getJdbcUrl(),
                                                           postgres.getUsername(),
                                                           postgres.getPassword(),
                                                           "data",
                                                           "infra",
                                                           "db/changelog/test-changelog.yaml",
                                                           1);

  static {
    DB_MANAGER.migrateDatabase();
    Aspects.aspectOf(TransactionalAspect.class).setDbManager(DB_MANAGER);
  }

  public static void beforeEach() {
    DB_MANAGER.pushTransaction();
  }

  public static void afterEach() {
    DB_MANAGER.popTransaction(true);
  }
}
