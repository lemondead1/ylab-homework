package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandTreeRoot;
import com.lemondead1.carshopservice.cli.command.Endpoint;

import java.util.HashMap;
import java.util.Map;

public class CommandRootBuilder implements TreeCommandBuilder<CommandRootBuilder> {
  private final Map<String, SubcommandBuilder<?, ?>> subcommands = new HashMap<>();

  public CommandTreeRoot build() {
    Map<String, Command> subcommands = new HashMap<>();
    for (var subcommand : this.subcommands.values()) {
      subcommands.put(subcommand.name, subcommand.build());
    }
    return new CommandTreeRoot(subcommands);
  }

  @Override
  public TreeSubcommandBuilder<CommandRootBuilder> push(String name) {
    return TreeSubcommandBuilder.pushImpl(this, subcommands, name);
  }

  @Override
  public EndpointSubcommandBuilder<CommandRootBuilder> accept(String name, Endpoint endpoint) {
    return TreeSubcommandBuilder.acceptImpl(this, subcommands, name, endpoint);
  }
}
