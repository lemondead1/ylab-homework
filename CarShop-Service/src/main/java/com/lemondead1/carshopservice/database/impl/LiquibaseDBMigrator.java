package com.lemondead1.carshopservice.database.impl;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.database.DBMigrator;
import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class LiquibaseDBMigrator implements DBMigrator {
  @Value("${database.schema}")
  private final String schema;

  @Value("${database.liquibase.schema}")
  private final String liquibaseSchema;

  @Value("${database.liquibase.changelog}")
  private final String changelogPath;

  private final DBManager dbManager;

  @EventListener
  public void onContextRefresh(ContextRefreshedEvent event) {
    migrateDatabase();
  }

  @Override
  public void migrateDatabase() {
    dbManager.pushTransaction();
    var conn = dbManager.getConnection();
    try (var stmt = conn.createStatement()) {
      stmt.execute("create schema if not exists " + schema);
      stmt.execute("create schema if not exists " + liquibaseSchema);

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);

      var liquibase = new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database);
      liquibase.update();
      dbManager.popTransaction(false);
    } catch (SQLException | LiquibaseException e) {
      dbManager.popTransaction(true);
      throw new DBException("Failed to init the database", e);
    }
  }
}
