package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import com.lemondead1.carshopservice.util.EnumUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum OrderState implements HasId {
  NEW("new", "New"),
  PERFORMING("performing", "Performing"),
  DONE("done", "Done"),
  CANCELLED("cancelled", "Cancelled");

  public static final List<OrderState> ALL = List.of(values());
  public static final Set<OrderState> ALL_SET = Set.of(values());

  private static final Map<String, OrderState> idToEnum = EnumUtil.createIdMap(OrderState.class);

  private final String id;
  private final String prettyName;

  public static OrderState parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
