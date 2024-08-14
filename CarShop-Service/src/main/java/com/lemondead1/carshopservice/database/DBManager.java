package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RequiredArgsConstructor
public class DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;
  private final String liquibaseSchema;
  private final boolean autocommit;

  private Connection connection;

  public void setupDatabase() {
    try {
      var conn = connect();
      conn.prepareStatement("create schema if not exists " + schema).execute();
      conn.prepareStatement("create schema if not exists " + liquibaseSchema).execute();

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);

      var liquibase = new Liquibase("db/changelog/changelog.yaml", new ClassLoaderResourceAccessor(), database);
      liquibase.update();
      conn.commit();
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }

  public void dropSchemas() {
    try (var conn = DriverManager.getConnection(url, user, password)) {
      conn.prepareStatement("drop schema if exists " + schema + ", " + liquibaseSchema + " cascade").execute();
    } catch (SQLException e) {
      throw new DBException("Failed to drop all", e);
    }
  }

  public Connection connect() throws SQLException {
    if (connection == null) {
      connection = DriverManager.getConnection(url, user, password);
    }
    connection.setSchema(schema);
    connection.setAutoCommit(autocommit);
    return connection;
  }

  public void commit() {
    if (connection != null) {
      try {
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public void rollback() {
    if (connection != null) {
      try {
        connection.rollback();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
