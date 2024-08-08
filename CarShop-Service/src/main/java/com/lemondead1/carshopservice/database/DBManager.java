package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;

  public DBManager(String url, String user, String password, String schema) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
    initLiquibase();
  }

  private void initLiquibase() {
    try (var conn = DriverManager.getConnection(url, user, password)) {
      conn.prepareStatement("create schema if not exists \"" + schema + "\";").execute();
      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      var liquibase = new Liquibase("db/changelog/changelog.yaml", new ClassLoaderResourceAccessor(), database);
      liquibase.update();
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }
}
