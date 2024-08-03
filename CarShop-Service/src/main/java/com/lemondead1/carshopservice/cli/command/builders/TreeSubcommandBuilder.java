package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandTree;
import com.lemondead1.carshopservice.cli.command.Endpoint;
import com.lemondead1.carshopservice.enums.UserRole;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TreeSubcommandBuilder<PARENT> extends SubcommandBuilder<TreeSubcommandBuilder<PARENT>, PARENT>
    implements TreeCommandBuilder<TreeSubcommandBuilder<PARENT>> {
  private final Map<String, SubcommandBuilder<?, ?>> subcommands = new LinkedHashMap<>();

  public TreeSubcommandBuilder(PARENT parent, String name) {
    super(parent, name);
  }

  @Override
  Command build() {
    return new CommandTree(subcommands.values().stream().map(SubcommandBuilder::build).toList(), name, description,
                           allowedRoles);
  }

  static <SELF extends TreeCommandBuilder<SELF>> TreeSubcommandBuilder<SELF> pushImpl(
      SELF self, Map<String, SubcommandBuilder<?, ?>> subcommands, String name
  ) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new TreeSubcommandBuilder<>(self, name);
    subcommands.put(name, subcommand);
    return subcommand;
  }

  static <SELF extends TreeCommandBuilder<SELF>> EndpointSubcommandBuilder<SELF> acceptImpl(
      SELF self, Map<String, SubcommandBuilder<?, ?>> subcommands, String name, Endpoint endpoint
  ) {
    if ("help".equals(name)) {
      throw new IllegalArgumentException("help is a reserved command name");
    }
    if (subcommands.containsKey(name)) {
      throw new IllegalArgumentException("Subcommand with name " + name + " already exists");
    }
    var subcommand = new EndpointSubcommandBuilder<>(self, name, endpoint);
    subcommands.put(name, subcommand);
    return subcommand;
  }

  public TreeSubcommandBuilder<TreeSubcommandBuilder<PARENT>> push(String name) {
    return pushImpl(this, subcommands, name);
  }

  public EndpointSubcommandBuilder<TreeSubcommandBuilder<PARENT>> accept(String name, Endpoint endpoint) {
    return acceptImpl(this, subcommands, name, endpoint);
  }

  @Override
  public TreeSubcommandBuilder<PARENT> allow(UserRole... roles) {
    return (TreeSubcommandBuilder<PARENT>) super.allow(roles);
  }

  @Override
  public TreeSubcommandBuilder<PARENT> describe(String description) {
    return (TreeSubcommandBuilder<PARENT>) super.describe(description);
  }
}
