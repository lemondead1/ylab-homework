package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.util.DateRange;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public enum DateRangeParser implements Parser<DateRange> {
  INSTANCE;

  @Override
  public DateRange parse(String string) {
    var split = string.split(" *- *");

    return switch (split.length) {
      case 1 -> {
        Instant value;
        try {
          value = Instant.parse(split[0]);
        } catch (DateTimeParseException e) {
          throw new ParsingException(split[0] + " is not a valid timestamp.", e);
        }
        yield new DateRange(value, value);
      }
      case 2 -> {
        Instant min;
        try {
          min = Instant.parse(split[0]);
        } catch (DateTimeParseException e) {
          throw new ParsingException(split[0] + " is not a valid timestamp.", e);
        }
        Instant max;
        try {
          max = Instant.parse(split[0]);
        } catch (DateTimeParseException e) {
          throw new ParsingException(split[1] + " is not a valid timestamp.", e);
        }
        yield new DateRange(min, max);
      }
      default -> throw new ParsingException("'" + string + "' is not a date range.");
    };
  }
}
