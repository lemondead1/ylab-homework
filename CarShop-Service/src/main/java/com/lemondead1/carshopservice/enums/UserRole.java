package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import org.eclipse.jetty.security.RolePrincipal;

import java.util.List;
import java.util.Map;

@Getter
public enum UserRole implements HasId, Comparable<UserRole> {
  CLIENT("client", "Client"),
  MANAGER("manager", "Manager"),
  ADMIN("admin", "Admin");

  public static final List<UserRole> ALL = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = Util.createIdToValueMap(CLIENT, MANAGER, ADMIN);

  private final String id;
  private final String prettyName;
  private final RolePrincipal principal;

  UserRole(String name, String prettyName) {
    this.id = name;
    this.prettyName = prettyName;
    this.principal = new RolePrincipal(name);
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
