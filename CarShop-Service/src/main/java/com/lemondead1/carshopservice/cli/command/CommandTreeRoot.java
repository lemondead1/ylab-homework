package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.service.SessionService;

import java.util.*;

public class CommandTreeRoot {
  private final Map<String, Command> subcommands = new LinkedHashMap<>();

  public CommandTreeRoot(Collection<Command> subcommands) {
    for (var command : subcommands) {
      this.subcommands.put(command.getName(), command);
    }
  }

  private void printHelp(SessionService session, ConsoleIO cli) {
    cli.println("Subcommands:");
    for (var subcommand : subcommands.values()) {
      if (subcommand.getAllowedRoles().contains(session.getCurrentUserRole())) {
        cli.println("  " + subcommand.getName() + ": " + subcommand.getDescription());
      }
    }
  }

  public void execute(SessionService currentUser, ConsoleIO cli, String... path) {
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
