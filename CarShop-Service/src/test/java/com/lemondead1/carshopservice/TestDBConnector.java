package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.database.impl.DBManagerImpl;
import com.lemondead1.carshopservice.database.impl.LiquibaseDBMigrator;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDBConnector {
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static {
    postgres.start();
  }

  public static final DBManagerImpl DB_MANAGER = new DBManagerImpl(postgres.getJdbcUrl(),
                                                                   postgres.getUsername(),
                                                                   postgres.getPassword(),
                                                                   "data",
                                                                   1);

  static {
    var migrator = new LiquibaseDBMigrator("data", "infra", "db/changelog/changelog.yaml", "test", DB_MANAGER);
    migrator.migrateDatabase();
  }

  public static void beforeEach() {
    DB_MANAGER.pushTransaction();
  }

  public static void afterEach() {
    DB_MANAGER.popTransaction(true);
  }
}
