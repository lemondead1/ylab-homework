package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.entity.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandTreeRoot {
  private final Map<String, Command> subcommands = new LinkedHashMap<>();

  public CommandTreeRoot(Collection<Command> subcommands) {
    for (var command : subcommands) {
      this.subcommands.put(command.getName(), command);
    }
  }

  private void printHelp(User currentUser, CLI cli) {
    cli.println("Subcommands:");
    for (var subcommand : subcommands.values()) {
      if (subcommand.getAllowedRoles().contains(currentUser.role())) {
        cli.println("  " + subcommand.getName() + ": " + subcommand.getDescription());
      }
    }
  }

  public void execute(User currentUser, CLI cli, String... path) {
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
