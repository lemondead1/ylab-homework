package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandEndpoint;
import com.lemondead1.carshopservice.cli.command.Endpoint;

public class EndpointSubcommandBuilder extends SubcommandBuilder<EndpointSubcommandBuilder> {
  private final Endpoint endpoint;

  EndpointSubcommandBuilder(TreeSubcommandBuilder parent, String name, Endpoint endpoint) {
    super(parent, name);
    this.endpoint = endpoint;
  }

  @Override
  Command build() {
    return new CommandEndpoint(name, description, allowedRoles, endpoint);
  }
}
