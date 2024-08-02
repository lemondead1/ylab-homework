package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Endpoint;

public interface TreeCommandBuilder<SELF extends TreeCommandBuilder<SELF>> {
  TreeSubcommandBuilder<SELF> push(String name);

  EndpointSubcommandBuilder<SELF> accept(String name, Endpoint endpoint);
}
