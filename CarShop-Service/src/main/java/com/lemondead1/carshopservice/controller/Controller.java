package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;

public interface Controller {
  /**
   * Adds some endpoints to the builder
   * @param builder a {@link TreeCommandBuilder}. Implementations must have no assumptions about its type.
   */
  void registerEndpoints(TreeCommandBuilder<?> builder);
}
