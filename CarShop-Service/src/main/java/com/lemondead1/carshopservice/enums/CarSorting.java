package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;
import com.lemondead1.carshopservice.dto.Car;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@RequiredArgsConstructor
public enum CarSorting implements HasId {
  NAME_ASC("name", Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase)),
  NAME_DESC("name_reversed", Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase).reversed()),
  PRODUCTION_YEAR_ASC("older_first", Comparator.comparingInt(Car::productionYear)),
  PRODUCTION_YEAR_DESC("newer_first", Comparator.comparingInt(Car::productionYear).reversed()),
  PRICE_ASC("cheaper_first", Comparator.comparingInt(Car::price)),
  PRICE_DESC("expensive_first", Comparator.comparingInt(Car::price).reversed());

  private final String id;
  @Getter
  private final Comparator<Car> sorter;

  @Override
  public String getId() {
    return id;
  }
}
