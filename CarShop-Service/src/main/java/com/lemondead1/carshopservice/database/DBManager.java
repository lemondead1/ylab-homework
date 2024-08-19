package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
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
    return resetConnection(DriverManager.getConnection(url, user, password));
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

    return transaction.getConnection();
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
      transaction.push();
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
    transaction.pop(rollback);
  }

  private class ThreadTransaction {
    private Connection connection;
    private int savepointDepth = 0;
    private final Deque<Savepoint> savepoints = new ArrayDeque<>();

    private void push() {
      log.debug("Scheduling a savepoint creation.");
      savepointDepth++;
    }

    private void pop(boolean rollback) {
      if (savepointDepth > 0) {
        popSavepoint(rollback);
      } else {
        popTransaction(rollback);
      }
    }

    private void popSavepoint(boolean rollback) {
      savepointDepth--;

      if (savepoints.size() > savepointDepth) {
        Savepoint lastSavepoint = savepoints.pop();
        if (rollback) {
          try {
            log.debug("Rolling back to the savepoint.");
            connection.rollback(lastSavepoint);
            connection.releaseSavepoint(lastSavepoint);
          } catch (SQLException e) {
            throw new DBException("Failed to rollback to the savepoint.", e);
          }
        } else {
          try {
            log.debug("Releasing the savepoint.");
            connection.releaseSavepoint(lastSavepoint);
          } catch (SQLException e) {
            throw new DBException("Failed to release the savepoint.", e);
          }
        }
      } else {
        log.debug("Popping a scheduled savepoint.");
      }
    }

    private void popTransaction(boolean rollback) {
      currentTransactions.remove();

      if (connection != null) {
        if (rollback) {
          try {
            log.debug("Rolling back a transaction.");
            connection.rollback();
          } catch (SQLException e) {
            throw new DBException("Failed to roll back the transaction.", e);
          }
        } else {
          try {
            log.debug("Committing a transaction.");
            connection.commit();
          } catch (SQLException e) {
            throw new DBException("Failed to commit the transaction.", e);
          }
        }

        try {
          returnConnectionToPool(connection);
        } catch (SQLException e) {
          log.error("Failed to return the connection to the pool.", e);
        }
      } else {
        log.debug("Removing an unused a transaction.");
      }
    }

    @Nonnull
    private Connection getConnection() {
      if (connection != null) {
        return connection;
      }

      log.debug("Assigning a connection to the current transaction.");
      try {
        Connection taken = freeConnections.poll(20, TimeUnit.MILLISECONDS);

        if (taken == null) {
          log.info("Establishing a new connection.");
          taken = createConnection();
        } else if (!taken.isValid(5)) {
          taken.close();
          log.warn("A connection has been broken.");
          taken = createConnection();
        }

        //Adding scheduled savepoints
        for (int i = savepoints.size(); i < savepointDepth; i++) {
          log.debug("Setting a late savepoint.");
          savepoints.push(taken.setSavepoint());
        }

        connection = taken;
        return taken;
      } catch (InterruptedException | SQLException e) {
        throw new DBException("Failed to prepare a connection.", e);
      }
    }
  }
}
