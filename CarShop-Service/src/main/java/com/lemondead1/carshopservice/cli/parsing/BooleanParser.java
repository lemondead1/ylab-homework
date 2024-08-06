package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

public enum BooleanParser implements Parser<Boolean> {
  THROW(false),
  DEFAULT_TO_FALSE(true);

  private final boolean defaultToNo;

  BooleanParser(boolean defaultToNo) {
    this.defaultToNo = defaultToNo;
  }

  @Override
  public Boolean parse(String string) {
    return switch (string.toLowerCase()) {
      case "y", "yes", "true" -> Boolean.TRUE;
      case "n", "no", "false" -> Boolean.FALSE;
      default -> {
        if (defaultToNo) {
          yield false;
        } else {
          throw new ParsingException(String.format("'%s' is not a boolean.", string));
        }
      }
    };
  }
}
