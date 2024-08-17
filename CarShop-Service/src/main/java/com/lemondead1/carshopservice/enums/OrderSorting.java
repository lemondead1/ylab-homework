package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum OrderSorting implements HasId {
  CREATED_AT_DESC("latest_first"),
  CREATED_AT_ASC("oldest_first"),
  CAR_NAME_DESC("car_name_reversed"),
  CAR_NAME_ASC("car_name");

  private static final Map<String, OrderSorting> idToEnum = Util.createIdToValueMap(values());

  private final String id;

  public static OrderSorting parse(String id) {
    var found = idToEnum.get(id);
    if (found == null) {
      throw new IllegalArgumentException();
    }
    return found;
  }
}
