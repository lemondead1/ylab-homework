package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.CommandTreeRoot;
import com.lemondead1.carshopservice.cli.command.Endpoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds {@link CommandTreeRoot}
 */
public class CommandRootBuilder implements TreeCommandBuilder<CommandRootBuilder> {
  private final Map<String, SubcommandBuilder<?, ?>> subcommands = new LinkedHashMap<>();

  public CommandTreeRoot build() {
    return new CommandTreeRoot(subcommands.values().stream().map(SubcommandBuilder::build).toList());
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
