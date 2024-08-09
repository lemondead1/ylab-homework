package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SessionService;

import java.util.Collection;

public interface Command {
  String getName();

  String getDescription();

  Collection<UserRole> getAllowedRoles();

  void execute(User currentUser, ConsoleIO cli, String... path);
}
