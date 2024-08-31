package com.lemondead1.carshopservice.database;

public interface DBMigrator {
  /**
   * Performs database migration.
   */
  void migrateDatabase();
}
