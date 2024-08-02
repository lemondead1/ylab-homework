package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.enums.UserRole;

import javax.annotation.Nullable;
import java.util.Collection;

public interface Command {
  @Nullable
  String getName();

  @Nullable
  String getDescription();

  Collection<UserRole> allowedRoles();

  void execute(SessionService currentUser, ConsoleIO cli, String... path);
}
