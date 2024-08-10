package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import com.lemondead1.carshopservice.entity.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@RequiredArgsConstructor
public enum OrderSorting implements HasId {
  CREATED_AT_DESC("latest_first", Comparator.comparing(Order::createdAt).reversed()),
  CREATED_AT_ASC("oldest_first", Comparator.comparing(Order::createdAt)),
  CAR_NAME_DESC("car_name_reversed",
                Comparator.comparing((Order o) -> o.car().getBrandModel(), String::compareToIgnoreCase).reversed()),
  CAR_NAME_ASC("car_name", Comparator.comparing((Order o) -> o.car().getBrandModel(), String::compareToIgnoreCase));

  private final String id;
  @Getter
  private final Comparator<Order> sorter;

  @Override
  public String getId() {
    return id;
  }
}
