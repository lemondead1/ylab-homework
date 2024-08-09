package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;
  private final String liquibaseSchema;

  public DBManager(String url, String user, String password, String schema, String liquibaseSchema) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
    this.liquibaseSchema = liquibaseSchema;
  }

  public void init() {
    try (var conn = DriverManager.getConnection(url, user, password)) {
      conn.prepareStatement("create schema if not exists " + schema).execute();
      conn.prepareStatement("create schema if not exists " + liquibaseSchema).execute();

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);
      var liquibase = new Liquibase("db/changelog/changelog.yaml", new ClassLoaderResourceAccessor(), database);
      liquibase.update();
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }

  public void dropAll() {
    try (var conn = DriverManager.getConnection(url, user, password)) {
      conn.prepareStatement("drop schema " + schema + " cascade").execute();
      conn.prepareStatement("drop schema " + liquibaseSchema + " cascade").execute();
    } catch (SQLException e) {
      throw new DBException("Failed to drop all", e);
    }
  }

  public Connection connect() throws SQLException {
    var connection = DriverManager.getConnection(url, user, password);
    connection.setSchema(schema);
    return connection;
  }
}
