package com.lemondead1.carshopservice.database.impl;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.exceptions.DBException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public class DBManagerImpl implements DBManager {
  private final String url;
  private final String user;
  private final String password;
  private final String schema;

  private final ThreadLocal<ThreadTransaction> currentTransactions = new ThreadLocal<>();
  private final BlockingQueue<Connection> freeConnections;

  public DBManagerImpl(@Value("${database.url}") String url,
                       @Value("${database.username}") String user,
                       @Value("${database.password}") String password,
                       @Value("${database.schema}") String schema,
                       @Value("${database.connectionPoolSize}") int connectionPoolSize) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
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

  @PreDestroy
  public void closeConnectionPool() {
    while (!freeConnections.isEmpty()) {
      try {
        freeConnections.poll().close();
      } catch (SQLException e) {
        throw new DBException("Failed to close a connection.", e);
      }
    }
  }

  @Override
  public Connection getConnection() {
    ThreadTransaction transaction = currentTransactions.get();

    if (transaction == null) {
      throw new DBException("No transaction has been started.");
    }

    return transaction.getConnection();
  }

  @Override
  public void pushTransaction() {
    ThreadTransaction transaction = currentTransactions.get();

    if (transaction == null) {
      log.debug("Creating a new transaction.");
      currentTransactions.set(new ThreadTransaction());
    } else {
      transaction.push();
    }
  }

  @Override
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

      if (savepoints.size() <= savepointDepth) {
        log.debug("Popping a scheduled savepoint.");
        return;
      }

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
    }

    private void popTransaction(boolean rollback) {
      currentTransactions.remove();

      if (connection == null) {
        log.debug("Removing an unused a transaction.");
        return;
      }

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
