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
import java.sql.Savepoint;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class manages JDBC connection pool and ongoing transactions.
 * It turned out to be pretty complicated.
 * However, now it supports nested transactions.
 */
@Slf4j
public class DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;
  private final String liquibaseSchema;
  private final String changelogPath;

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
    if (freeConnections.offer(conn)) {
      log.debug("Returned a connection to the pool.");
    } else {
      conn.close();
      log.debug("Closing an excess connection.");
    }
  }

  public void migrateDatabase() {
    pushTransaction();
    var conn = getConnection();
    try (var stmt = conn.createStatement()) {
      stmt.execute("create schema if not exists " + schema);
      stmt.execute("create schema if not exists " + liquibaseSchema);

      var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      database.setDefaultSchemaName(schema);
      database.setLiquibaseSchemaName(liquibaseSchema);

      var liquibase = new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database);
      liquibase.update();
      popTransaction(false);
    } catch (SQLException | LiquibaseException e) {
      throw new DBException("Failed to init the database", e);
    }
  }

  /**
   * Returns a connection assigned to current transaction or assigns a new one and checks its validity.
   *
   * @return A connection. It should not be closed.
   *
   * @throws DBException if a transaction has not been started from this thread.
   */
  public Connection getConnection() {
    var transaction = currentTransactions.get();

    if (transaction == null) {
      throw new DBException("A transaction has not been started.");
    }

    if (transaction.connection != null) {
      return transaction.connection;
    }

    log.debug("Assigning a connection to current transaction.");
    Connection taken;
    try {
      taken = freeConnections.poll(20, TimeUnit.MILLISECONDS);

      if (taken == null) {
        log.info("Creating a new connection.");
        taken = resetConnection(createConnection());
      } else if (!taken.isValid(5)) {
        taken.close();
        log.warn("A connection has been broken.");
        taken = resetConnection(createConnection());
      }

      for (int i = 0; i < transaction.startingSavepointDepth; i++) {
        log.debug("Setting a late savepoint.");
        var savepoint = taken.setSavepoint();
        transaction.savepoints.push(savepoint);
      }

    } catch (InterruptedException | SQLException e) {
      throw new DBException("Failed to prepare a connection.", e);
    }
    transaction.connection = taken;
    return taken;
  }

  public void pushTransaction() {
    ThreadTransaction transaction = currentTransactions.get();

    if (transaction == null) {
      log.debug("Creating a new transaction.");
      currentTransactions.set(new ThreadTransaction());
    } else if (transaction.connection == null) {
      log.debug("Scheduling savepoint creation.");
      transaction.startingSavepointDepth++;
    } else {
      log.debug("Setting a savepoint.");
      Savepoint savepoint;
      try {
        savepoint = transaction.connection.setSavepoint();
      } catch (SQLException e) {
        throw new DBException("Failed to set a savepoint.", e);
      }
      transaction.savepoints.push(savepoint);
    }
  }

  public void popTransaction(boolean rollback) {
    var transaction = currentTransactions.get();
    if (transaction == null) {
      log.warn("A pop was called from a thread which has not started any transactions.");
    } else if (transaction.connection == null) {
      if (transaction.startingSavepointDepth > 0) {
        log.debug("Popping a scheduled savepoint.");
        transaction.startingSavepointDepth--;
      } else {
        log.debug("Removing an unused transaction.");
        currentTransactions.remove();
      }
    } else if (transaction.savepoints.isEmpty()) {
      if (rollback) {
        try {
          log.debug("Rolling back a transaction.");
          transaction.connection.rollback();
          returnConnectionToPool(transaction.connection);
          currentTransactions.remove();
        } catch (SQLException e) {
          throw new DBException("Failed to roll back the transaction.", e);
        }
      } else {
        try {
          log.debug("Committing a transaction.");
          transaction.connection.commit();
          returnConnectionToPool(transaction.connection);
          currentTransactions.remove();
        } catch (SQLException e) {
          throw new DBException("Failed to commit the transaction.", e);
        }
      }
    } else {
      var lastSavepoint = transaction.savepoints.pop();
      if (rollback) {
        try {
          log.debug("Rolling back to a savepoint.");
          transaction.connection.rollback(lastSavepoint);
        } catch (SQLException e) {
          throw new DBException("Failed to rollback to the savepoint.", e);
        }
      } else {
        try {
          log.debug("Releasing a savepoint.");
          transaction.connection.releaseSavepoint(lastSavepoint);
        } catch (SQLException e) {
          throw new DBException("Failed to release th savepoint.", e);
        }
      }
    }
  }

  private static class ThreadTransaction {
    private Connection connection;
    private int startingSavepointDepth = 0;
    private final Deque<Savepoint> savepoints = new ArrayDeque<>();
  }
}
