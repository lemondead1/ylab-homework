package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.util.IntRange;

public enum IntRangeParser implements Parser<IntRange> {
  INSTANCE;

  @Override
  public IntRange parse(String string) {
    var split = string.split(" *- *");

    return switch (split.length) {
      case 1 -> {
        int value;
        try {
          value = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
          throw new ParsingException("'" + split[0] + "' is not an integer.", e);
        }
        yield new IntRange(value, value);
      }
      case 2 -> {
        int min;
        try {
          min = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
          throw new ParsingException("'" + split[0] + "' is not an integer.", e);
        }
        int max;
        try {
          max = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
          throw new ParsingException("'" + split[1] + "' is not an integer.", e);
        }
        yield new IntRange(min, max);
      }
      default -> throw new ParsingException("'" + string + "' is not an integer range.");
    };
  }
}
