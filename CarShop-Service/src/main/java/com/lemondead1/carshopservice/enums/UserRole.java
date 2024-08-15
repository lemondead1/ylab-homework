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
public class UserRole extends RolePrincipal implements HasId {
  public static final UserRole ANONYMOUS = new UserRole("anonymous", "Anonymous");
  public static final UserRole CLIENT = new UserRole("client", "Client");
  public static final UserRole MANAGER = new UserRole("manager", "Manager");
  public static final UserRole ADMIN = new UserRole("admin", "Admin");

  public static final List<UserRole> AUTHORIZED = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = Util.createIdToValueMap(ANONYMOUS, CLIENT, MANAGER, ADMIN);

  private final String id;
  private final String prettyName;

  private UserRole(String name, String prettyName) {
    super(name);
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
}
