package com.lemondead1.carshopservice.enums;


import com.fasterxml.jackson.annotation.JsonValue;
import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import org.eclipse.jetty.security.RolePrincipal;

import java.util.List;
import java.util.Map;

public enum UserRole implements HasId {
  CLIENT("client"),
  MANAGER("manager"),
  ADMIN("admin");

  public static final List<UserRole> ALL = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = Util.createIdToValueMap(CLIENT, MANAGER, ADMIN);

  private final String id;
  private final RolePrincipal principal;

  UserRole(String name) {
    this.id = name;
    this.principal = new RolePrincipal(name);
  }

  @JsonValue
  @Override
  public String getId() {
    return this.id;
  }

  public RolePrincipal toPrincipal() {
    return principal;
  }

  public static UserRole parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
