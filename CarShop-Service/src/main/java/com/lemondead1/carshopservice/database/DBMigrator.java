package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.annotations.Transactional;
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
public class DBMigrator {
  @Value("${database.schema}")
  private final String schema;

  @Value("${database.liquibase.schema}")
  private final String liquibaseSchema;

  @Value("${database.liquibase.changelog}")
  private final String changelogPath;

  private final DBManager dbManager;

  @EventListener
  @Transactional
  public void onContextRefresh(ContextRefreshedEvent event) {
    migrateDatabase();
  }

  public void migrateDatabase() {
    var conn = dbManager.getConnection();
    try (var stmt = conn.createStatement()) {
      stmt.execute("create schema if not exists " + schema);
      stmt.execute("create schema if not exists " + liquibaseSchema);

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);

      var liquibase = new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database);
      liquibase.update();
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }
}
