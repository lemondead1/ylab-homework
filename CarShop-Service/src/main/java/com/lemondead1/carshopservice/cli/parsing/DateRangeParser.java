package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.util.DateRange;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DateRangeParser implements Parser<DateRange> {
  public static final DateRangeParser INSTANCE = new DateRangeParser(ZoneId::systemDefault);

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M[.uuuu][.uu][ HH:mm:ss]");
  private final Supplier<ZoneId> zoneSupplier;

  private Instant parse(String string, boolean start) {
    try {
      var best = formatter.parseBest(string, LocalDateTime::from, LocalDate::from);
      if (best instanceof LocalDate date) {
        return start ? date.atTime(0, 0, 0, 0).atZone(zoneSupplier.get()).toInstant()
                     : date.atTime(23, 59, 59, 999_999_999).atZone(zoneSupplier.get()).toInstant();
      } else {
        return ((LocalDateTime) best).atZone(zoneSupplier.get()).toInstant();
      }
    } catch (DateTimeParseException e) {
      throw new ParsingException(string + " is not a valid timestamp.", e);
    }
  }

  @Override
  public DateRange parse(String string) {
    var split = string.split(" *- *");

    Instant min;
    Instant max;

    switch (split.length) {
      case 1 -> {
        min = parse(split[0], true);
        max = parse(split[0], false);
      }
      case 2 -> {
        min = parse(split[0], true);
        max = parse(split[1], false);
      }
      default -> throw new ParsingException("'" + string + "' is not a date range.");
    }

    if (min.isAfter(max)) {
      throw new ParsingException("Span start is later than span end.");
    }

    return new DateRange(min, max);
  }
}
