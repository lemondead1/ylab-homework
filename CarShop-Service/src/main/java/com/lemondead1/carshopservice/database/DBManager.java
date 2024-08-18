package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class manages JDBC connection pool and ongoing transactions.
 * It turned out to be pretty complicated.
 * However, it still does not support nested transactions.
 */
@Slf4j
public class DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;
  private final String liquibaseSchema;
  private final String changelogPath;
  private final int connectionPoolSize;

  private final ThreadLocal<ThreadTransaction> currentTransactions = new ThreadLocal<>();
  private final BlockingQueue<Connection> freeConnections;

  public DBManager(String url,
                   String user,
                   String password,
                   String schema,
                   String liquibaseSchema,
                   String changelogPath,
                   int connectionPoolSize) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
    this.liquibaseSchema = liquibaseSchema;
    this.changelogPath = changelogPath;
    this.connectionPoolSize = connectionPoolSize;
    freeConnections = new ArrayBlockingQueue<>(connectionPoolSize);
  }

  private Connection resetConnection(Connection conn) throws SQLException {
    conn.setSchema(schema);
    return conn;
  }

  private Connection createConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }

  private void returnConnectionToPool(Connection conn) throws SQLException {
    if (conn.isClosed()) {
      log.warn("Tried to return a closed connection to the pool.");
      conn = createConnection();
    }

    resetConnection(conn);
    freeConnections.add(conn);
  }

  public void populateConnectionPool() {
    try {
      for (int i = 0; i < connectionPoolSize; i++) {
        returnConnectionToPool(createConnection());
      }
    } catch (SQLException e) {
      throw new DBException("Failed to initialize connection pool.", e);
    }
  }

  public void setupDatabase() {
    startTransaction();
    var conn = getConnection();
    try (var stmt = conn.createStatement()) {
      stmt.execute("create schema if not exists " + schema);
      stmt.execute("create schema if not exists " + liquibaseSchema);

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);

      var liquibase = new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database);
      liquibase.update();
      commit();
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }

  public Connection getConnection() {
    var transaction = currentTransactions.get();

    if (transaction == null) {
      throw new DBException("A transaction has not been started.");
    }

    if (transaction.connection != null) {
      return transaction.connection;
    }

    Connection taken;
    try {
      taken = freeConnections.poll(100, TimeUnit.MILLISECONDS);

      if (taken == null) {
        log.warn("A connection was leaked.");
        taken = resetConnection(createConnection());
      } else if (!taken.isValid(5)) {
        taken.close();
        log.warn("A connection has been broken.");
        taken = resetConnection(createConnection());
      }
    } catch (InterruptedException | SQLException e) {
      throw new RuntimeException(e);
    }
    transaction.connection = taken;
    return taken;
  }

  public void startTransaction() {
    log.debug("Starting a transaction.");
    if (currentTransactions.get() != null) {
      log.warn("Previous transaction has not been closed.");
    }
    currentTransactions.set(new ThreadTransaction());
  }

  public void commit() {
    log.debug("Committing a transaction.");
    var transaction = currentTransactions.get();
    if (transaction == null) {
      log.warn("A commit was called from a thread which has not started any transactions.");
      return;
    }
    if (transaction.connection == null) {
      return;
    }
    try {
      transaction.connection.commit();
      returnConnectionToPool(transaction.connection);
      currentTransactions.remove();
    } catch (SQLException e) {
      log.error("An exception was encountered while committing a transaction.", e);
    }
  }

  public void rollback() {
    log.debug("Rolling back a transaction.");
    var transaction = currentTransactions.get();
    if (transaction == null) {
      log.warn("A rollback was called from a thread which has not started any connections.");
      return;
    }
    if (transaction.connection == null) {
      return;
    }
    try {
      transaction.connection.rollback();
      returnConnectionToPool(transaction.connection);
      currentTransactions.remove();
    } catch (SQLException e) {
      log.error("An exception was encountered while rolling back a transaction.", e);
    }
  }

  private static class ThreadTransaction {
    private Connection connection;
  }
}
