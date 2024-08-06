package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

public enum IntParser implements Parser<Integer> {
  INSTANCE;

  @Override
  public Integer parse(String string) {
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      throw new ParsingException("'" + string + "' is not an integer.", e);
    }
  }
}
