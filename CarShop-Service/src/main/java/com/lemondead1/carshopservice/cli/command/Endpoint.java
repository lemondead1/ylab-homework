package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.entity.User;

import javax.annotation.Nullable;

/**
 * Processes incoming user commands at a certain path
 */
public interface Endpoint {
  @Nullable
  String execute(User currentUser, ConsoleIO cli, String... args);
}
