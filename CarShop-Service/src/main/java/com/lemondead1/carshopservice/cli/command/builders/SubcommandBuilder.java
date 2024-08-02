package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.enums.UserRole;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

abstract class SubcommandBuilder<SELF extends SubcommandBuilder<SELF>> {
  final TreeSubcommandBuilder parent;
  final String name;
  String description;
  final Set<UserRole> allowedRoles = EnumSet.noneOf(UserRole.class);

  SubcommandBuilder(TreeSubcommandBuilder parent, String name) {
    this.parent = parent;
    description = name;
    this.name = name;
  }

  public TreeSubcommandBuilder pop() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  public SELF describe(String description) {
    this.description = description;
    return (SELF) this;
  }

  @SuppressWarnings("unchecked")
  public SELF allow(UserRole... roles) {
    allowedRoles.addAll(Arrays.asList(roles));
    return (SELF) this;
  }

  abstract Command build();
}
