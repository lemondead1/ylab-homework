package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;

public interface Controller {
  void registerEndpoints(TreeSubcommandBuilder builder);
}
