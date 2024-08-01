package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandTree;
import com.lemondead1.carshopservice.cli.command.Endpoint;
import com.lemondead1.carshopservice.enums.UserRole;

import java.util.HashMap;
import java.util.Map;

public class TreeSubcommandBuilder extends SubcommandBuilder {
  private final Map<String, SubcommandBuilder> subcommands = new HashMap<>();

  TreeSubcommandBuilder(TreeSubcommandBuilder parent, String name, String description) {
    super(parent, name, description);
  }

  @Override
  public TreeSubcommandBuilder allow(UserRole... roles) {
    return (TreeSubcommandBuilder) super.allow(roles);
  }

  @Override
  Command build() {
    Map<String, Command> subcommands = new HashMap<>();
    for (var subcommand : this.subcommands.values()) {
      subcommands.put(subcommand.name, subcommand.build());
    }
    return new CommandTree(subcommands, name, description, allowedRoles);
  }

  public TreeSubcommandBuilder push(String name, String description) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new TreeSubcommandBuilder(this, name, description);
    subcommands.put(name, subcommand);
    return subcommand;
  }

  public EndpointSubcommandBuilder accept(String name, String description, Endpoint endpoint) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new EndpointSubcommandBuilder(this, name, description, endpoint);
    subcommands.put(name, subcommand);
    return subcommand;
  }
}
