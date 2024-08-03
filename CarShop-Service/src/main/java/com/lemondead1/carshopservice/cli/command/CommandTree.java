package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SessionService;

import java.util.*;

public class CommandTree implements Command {
  private final Map<String, Command> subcommands = new LinkedHashMap<>();
  private final String name;
  private final String description;
  private final Set<UserRole> allowedRoles;

  public CommandTree(Collection<Command> subcommands, String name, String description, Set<UserRole> allowedRoles) {
    for (var subcommand : subcommands) {
      this.subcommands.put(subcommand.getName(), subcommand);
    }
    this.name = name;
    this.description = description;
    this.allowedRoles = allowedRoles;
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
  public Collection<UserRole> getAllowedRoles() {
    return allowedRoles;
  }

  private void printHelp(SessionService session, ConsoleIO cli) {
    cli.println(description);
    cli.println("Subcommands:");
    for (var subcommand : subcommands.values()) {
      if (subcommand.getAllowedRoles().contains(session.getCurrentUserRole())) {
        cli.println("  " + subcommand.getName() + ": " + subcommand.getDescription());
      }
    }
  }

  @Override
  public void execute(SessionService currentUser, ConsoleIO cli, String... path) {
    if (!getAllowedRoles().contains(currentUser.getCurrentUserRole())) {
      cli.println("Insufficient permissions.");
      return;
    }

    if (path.length == 0) {
      printHelp(currentUser, cli);
      return;
    }

    var subcommandName = path[0];
    if ("help".equals(subcommandName)) {
      printHelp(currentUser, cli);
      return;
    }

    var subcommand = subcommands.get(subcommandName);
    if (subcommand == null) {
      cli.println("Command '" + subcommandName + "' not found. Use 'help' to list available commands.");
      return;
    }

    subcommand.execute(currentUser, cli, Arrays.copyOfRange(path, 1, path.length));
  }
}
