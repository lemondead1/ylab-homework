package com.lemondead1.carshopservice.enums;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public enum CarSorting implements HasId {
  NAME_ASC("alphabetically_reversed"),
  NAME_DESC("alphabetically"),
  YEAR_OF_ISSUE_ASC("older_first"),
  YEAR_OF_ISSUE_DESC("newer_first"),
  PRICE_ASC("cheaper_first"),
  PRICE_DESC("expensive_first");

  private final String id;

  CarSorting(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
