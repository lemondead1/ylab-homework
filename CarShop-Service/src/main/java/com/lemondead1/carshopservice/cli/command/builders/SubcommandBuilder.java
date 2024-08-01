package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.enums.UserRole;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

abstract class SubcommandBuilder {
  final TreeSubcommandBuilder parent;
  final String name;
  final String description;
  final Set<UserRole> allowedRoles = EnumSet.noneOf(UserRole.class);

  SubcommandBuilder(TreeSubcommandBuilder parent, String name, String description) {
    this.parent = parent;
    this.name = name;
    this.description = description;
  }

  public TreeSubcommandBuilder pop() {
    return parent;
  }

  public SubcommandBuilder allow(UserRole... roles) {
    allowedRoles.addAll(Arrays.asList(roles));
    return this;
  }

  abstract Command build();
}
