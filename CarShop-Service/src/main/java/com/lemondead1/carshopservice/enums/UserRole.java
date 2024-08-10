package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import com.lemondead1.carshopservice.util.EnumUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum UserRole implements HasId {
  ANONYMOUS("anonymous", "Anonymous"),
  CLIENT("client", "Client"),
  MANAGER("manager", "Manager"),
  ADMIN("admin", "Admin");

  public static final List<UserRole> AUTHORIZED = List.of(CLIENT, MANAGER, ADMIN);

  private static final Map<String, UserRole> idToEnum = EnumUtil.createIdMap(UserRole.class);

  private final String id;
  private final String prettyName;

  public static UserRole parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
