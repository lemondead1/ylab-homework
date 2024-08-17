package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import org.eclipse.jetty.security.RolePrincipal;

import java.util.List;
import java.util.Map;

/**
 * It's a shame they decided to make RolePrincipal a class.
 * However, I'll leave UserRole in the enums package anyway.
 */
@Getter
public class UserRole extends RolePrincipal implements HasId, Comparable<UserRole> {
  public static final UserRole CLIENT = new UserRole(0, "client", "Client");
  public static final UserRole MANAGER = new UserRole(1, "manager", "Manager");
  public static final UserRole ADMIN = new UserRole(2, "admin", "Admin");

  public static final List<UserRole> ALL = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = Util.createIdToValueMap(CLIENT, MANAGER, ADMIN);

  private final int ordinal;
  private final String id;
  private final String prettyName;

  private UserRole(int ordinal, String name, String prettyName) {
    super(name);
    this.ordinal = ordinal;
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
  public int compareTo(UserRole o) {
    return Integer.compare(ordinal, o.ordinal);
  }
}
