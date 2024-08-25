package com.lemondead1.carshopservice.database;

import com.lemondead1.carshopservice.exceptions.DBException;

import java.sql.Connection;

public interface DBManager {
  /**
   * Returns a connection assigned to current transaction or assigns a new one and checks its validity.
   *
   * @return A connection. It should not be closed.
   * @throws DBException if a transaction has not been started from this thread.
   */
  Connection getConnection();

  /**
   * Creates a new transaction or adds a savepoint lazily.
   */
  void pushTransaction();

  /**
   * Commits/rolls back latest transaction/savepoint.
   *
   * @param rollback {@code true} to rollback.
   */
  void popTransaction(boolean rollback);
}
