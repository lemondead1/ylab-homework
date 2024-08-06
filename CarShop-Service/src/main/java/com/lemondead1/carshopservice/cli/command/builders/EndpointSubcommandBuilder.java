package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.cli.command.CommandEndpoint;
import com.lemondead1.carshopservice.cli.command.Endpoint;
import com.lemondead1.carshopservice.enums.UserRole;

public class EndpointSubcommandBuilder<PARENT> extends SubcommandBuilder<EndpointSubcommandBuilder<PARENT>, PARENT> {
  private final Endpoint endpoint;

  EndpointSubcommandBuilder(PARENT parent, String name, Endpoint endpoint) {
    super(parent, name);
    this.endpoint = endpoint;
  }

  @Override
  Command build() {
    return new CommandEndpoint(name, description, allowedRoles, endpoint);
  }

  @Override
  public EndpointSubcommandBuilder<PARENT> allow(UserRole... roles) {
    return (EndpointSubcommandBuilder<PARENT>) super.allow(roles);
  }

  @Override
  public EndpointSubcommandBuilder<PARENT> describe(String description) {
    return (EndpointSubcommandBuilder<PARENT>) super.describe(description);
  }
}
