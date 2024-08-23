package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import org.eclipse.jetty.security.RolePrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

@Getter
public enum UserRole implements HasId, GrantedAuthority {
  CLIENT("client", "Client"),
  MANAGER("manager", "Manager"),
  ADMIN("admin", "Admin");

  public static final List<UserRole> ALL = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = Util.createIdToValueMap(CLIENT, MANAGER, ADMIN);

  private final String id;
  private final String prettyName;

  UserRole(String name, String prettyName) {
    this.id = name;
    this.prettyName = prettyName;
  }

  public static UserRole parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }

  @Override
  public String getAuthority() {
    return id;
  }
}
