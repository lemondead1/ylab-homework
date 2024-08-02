package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandTree;
import com.lemondead1.carshopservice.cli.command.Endpoint;

import java.util.HashMap;
import java.util.Map;

public class TreeSubcommandBuilder extends SubcommandBuilder<TreeSubcommandBuilder> {
  private final Map<String, SubcommandBuilder<?>> subcommands = new HashMap<>();

  TreeSubcommandBuilder(TreeSubcommandBuilder parent, String name) {
    super(parent, name);
  }

  @Override
  Command build() {
    Map<String, Command> subcommands = new HashMap<>();
    for (var subcommand : this.subcommands.values()) {
      subcommands.put(subcommand.name, subcommand.build());
    }
    return new CommandTree(subcommands, name, description, allowedRoles);
  }

  public TreeSubcommandBuilder push(String name) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new TreeSubcommandBuilder(this, name);
    subcommands.put(name, subcommand);
    return subcommand;
  }

  public EndpointSubcommandBuilder accept(String name, Endpoint endpoint) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new EndpointSubcommandBuilder(this, name, endpoint);
    subcommands.put(name, subcommand);
    return subcommand;
  }
}
