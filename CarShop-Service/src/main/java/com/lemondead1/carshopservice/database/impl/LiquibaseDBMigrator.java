package com.lemondead1.carshopservice.database.impl;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.database.DBMigrator;
import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiquibaseDBMigrator implements DBMigrator {
  @Value("${database.schema}")
  private final String schema;

  @Value("${database.liquibase.schema}")
  private final String liquibaseSchema;

  @Value("${database.liquibase.changelog}")
  private final String changelogPath;

  @Value("${database.liquibase.context:default}")
  private final String context;

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

      new CommandScope("update").addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogPath)
                                .addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, context)
                                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                                .execute();
      dbManager.popTransaction(false);
    } catch (SQLException | LiquibaseException e) {
      dbManager.popTransaction(true);
      throw new DBException("Failed to init the database", e);
    }
  }
}
