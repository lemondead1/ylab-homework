package com.lemondead1.carshopservice.util;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

/**
 * Inclusive range of timestamps.
 */
public record DateRange(Instant min, Instant max) implements Predicate<Instant> {
  //Reduced date range to accommodate for Postgres
  public static final DateRange ALL = new DateRange(
      OffsetDateTime.of(-4713, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
      OffsetDateTime.of(294276, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
  );

  public DateRange {
    if (min.isAfter(max)) {
      throw new IllegalArgumentException(min + " is after " + max);
    }
  }

  @Override
  public boolean test(Instant instant) {
    return !min.isAfter(instant) && !instant.isAfter(max);
  }
}