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
 * This class manages a JDBC connection pool and ongoing transactions.
 * The connection pool is populated lazily, up to {@code connectionPoolSize} connections.
 * Supports nested transactions.
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
    conn.setAutoCommit(false);
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
      log.debug("Returned the connection to the pool.");
    } else {
      conn.close();
      log.debug("Closing an excess connection.");
    }
  }

  public void closeConnectionPool() {
    while (!freeConnections.isEmpty()) {
      try {
        freeConnections.poll().close();
      } catch (SQLException e) {
        throw new DBException("Failed to close a connection.", e);
      }
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
   * @throws DBException if a transaction has not been started from this thread.
   */
  public Connection getConnection() {
    ThreadTransaction transaction = currentTransactions.get();

    if (transaction == null) {
      throw new DBException("No transaction has been started.");
    }

    if (transaction.connection != null) {
      return transaction.connection;
    }

    log.debug("Assigning a connection to current transaction.");
    Connection taken;
    try {
      taken = freeConnections.poll(20, TimeUnit.MILLISECONDS);

      if (taken == null) {
        log.info("Establishing a new connection.");
        taken = resetConnection(createConnection());
      } else if (!taken.isValid(5)) {
        taken.close();
        log.warn("A connection has been broken.");
        taken = resetConnection(createConnection());
      }

      //Adding savepoints lazily
      for (int i = transaction.savepoints.size(); i < transaction.savepointDepth; i++) {
        log.debug("Setting a late savepoint.");
        transaction.savepoints.push(taken.setSavepoint());
      }

    } catch (InterruptedException | SQLException e) {
      throw new DBException("Failed to prepare a connection.", e);
    }
    transaction.connection = taken;
    return taken;
  }

  /**
   * Creates a new transaction or adds a savepoint lazily.
   */
  public void pushTransaction() {
    ThreadTransaction transaction = currentTransactions.get();

    if (transaction == null) {
      log.debug("Creating a new transaction.");
      currentTransactions.set(new ThreadTransaction());
    } else {
      log.debug("Scheduling savepoint creation.");
      transaction.savepointDepth++;
    }
  }

  /**
   * Commits/rolls back latest transaction/savepoint.
   *
   * @param rollback {@code true} to rollback.
   */
  public void popTransaction(boolean rollback) {
    var transaction = currentTransactions.get();
    if (transaction == null) {
      log.warn("A pop was called from a thread which has not started any transactions.");
      return;
    }

    if (transaction.savepointDepth > 0) {
      transaction.savepointDepth--;

      if (transaction.savepoints.size() > transaction.savepointDepth) {
        var lastSavepoint = transaction.savepoints.pop();
        if (rollback) {
          try {
            log.debug("Rolling back to a savepoint.");
            transaction.connection.rollback(lastSavepoint);
            transaction.connection.releaseSavepoint(lastSavepoint);
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
      } else {
        log.debug("Popping a scheduled savepoint.");
      }

      if (transaction.savepoints.size() > transaction.savepointDepth) {
        throw new DBException("Savepoint stack is broken.");
      }
    } else {
      currentTransactions.remove();

      if (transaction.connection != null) {
        if (rollback) {
          try {
            log.debug("Rolling back a transaction.");
            transaction.connection.rollback();
          } catch (SQLException e) {
            throw new DBException("Failed to roll back the transaction.", e);
          }
        } else {
          try {
            log.debug("Committing a transaction.");
            transaction.connection.commit();
          } catch (SQLException e) {
            throw new DBException("Failed to commit the transaction.", e);
          }
        }

        try {
          returnConnectionToPool(transaction.connection);
        } catch (SQLException e) {
          log.error("Failed to return the connection to the pool.", e);
        }
      } else {
        log.debug("Removing an unused a transaction.");
      }
    }
  }

  private static class ThreadTransaction {
    private Connection connection;
    private int savepointDepth = 0;
    private final Deque<Savepoint> savepoints = new ArrayDeque<>();
  }
}
