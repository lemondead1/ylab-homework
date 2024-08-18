package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.database.DBManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * An easy way to reset all changes that were done by the test code.
 * I don't know how to feel about this.
 */
public class TestDBManager extends DBManager {
  private final Connection connection;

  public TestDBManager(String url,
                       String user,
                       String password,
                       String schema,
                       String liquibaseSchema,
                       String changelogPath) {
    super(url, user, password, schema, liquibaseSchema, changelogPath, 1);
    try {
      connection = DriverManager.getConnection(url, user, password);
      connection.setAutoCommit(true);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Connection getConnection() {
    return connection;
  }

  @Override
  public void commit() { }

  @Override
  public void rollback() {
    try {
      connection.rollback();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void startTransaction() { }

  public void prepareForTests() {
    super.setupDatabase();
    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
