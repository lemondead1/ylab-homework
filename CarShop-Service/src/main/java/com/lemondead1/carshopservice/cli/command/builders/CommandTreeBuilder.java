package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.enums.UserRole;

public class CommandTreeBuilder extends TreeSubcommandBuilder {
  public CommandTreeBuilder() {
    super(null, null);
    for (var role : UserRole.values()) {
      allow(role);
    }
  }

  @Override
  public Command build() {
    return super.build();
  }
}
