package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.service.SessionService;

import java.util.Collection;
import java.util.Set;

public class CommandEndpoint implements Command {
  private final String name;
  private final String description;
  private final Set<UserRole> allowedRoles;
  private final Endpoint endpoint;

  public CommandEndpoint(String name, String description, Set<UserRole> allowedRoles, Endpoint endpoint) {
    this.name = name;
    this.description = description;
    this.allowedRoles = allowedRoles;
    this.endpoint = endpoint;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Collection<UserRole> allowedRoles() {
    return allowedRoles;
  }

  @Override
  public void execute(SessionService currentUser, ConsoleIO cli, String... path) {
    try {
      if (path.length >= 1 && "help".equals(path[0])) {
        cli.println(getDescription());
      } else {
        var result = endpoint.execute(currentUser, cli, path);
        cli.println(result);
      }
    } catch (CommandException e) {
      cli.println(e.getMessage());
    }
  }
}
