package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.util.HasId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSorting implements HasId {
  CREATED_AT_DESC("latest_first"),
  CREATED_AT_ASC("oldest_first"),
  CAR_NAME_DESC("car_name_reversed"),
  CAR_NAME_ASC("car_name");

  private final String id;
}
