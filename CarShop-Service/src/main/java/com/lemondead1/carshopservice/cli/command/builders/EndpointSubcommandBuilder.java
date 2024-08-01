package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandEndpoint;
import com.lemondead1.carshopservice.cli.command.Endpoint;
import com.lemondead1.carshopservice.enums.UserRole;

public class EndpointSubcommandBuilder extends SubcommandBuilder {
  private final Endpoint endpoint;

  EndpointSubcommandBuilder(TreeSubcommandBuilder parent, String name,
                            String description, Endpoint endpoint) {
    super(parent, name, description);
    this.endpoint = endpoint;
  }

  @Override
  public EndpointSubcommandBuilder allow(UserRole... userRoles) {
    return (EndpointSubcommandBuilder) super.allow(userRoles);
  }

  @Override
  Command build() {
    return new CommandEndpoint(name, description, allowedRoles, endpoint);
  }
}
