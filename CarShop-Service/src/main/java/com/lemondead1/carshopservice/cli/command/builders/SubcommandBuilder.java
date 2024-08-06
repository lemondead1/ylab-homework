package com.lemondead1.carshopservice.cli.command.builders;

import com.lemondead1.carshopservice.cli.command.Command;
import com.lemondead1.carshopservice.enums.UserRole;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

abstract class SubcommandBuilder<SELF extends SubcommandBuilder<SELF, PARENT>, PARENT> {
  final PARENT parent;
  final String name;
  String description;
  final Set<UserRole> allowedRoles = EnumSet.noneOf(UserRole.class);

  SubcommandBuilder(PARENT parent, String name) {
    this.parent = parent;
    description = name;
    this.name = name;
  }

  public PARENT pop() {
    return parent;
  }

  SubcommandBuilder<SELF, PARENT> describe(String description) {
    this.description = description;
    return this;
  }

  SubcommandBuilder<SELF, PARENT> allow(UserRole... roles) {
    allowedRoles.addAll(Arrays.asList(roles));
    return this;
  }

  abstract Command build();
}
