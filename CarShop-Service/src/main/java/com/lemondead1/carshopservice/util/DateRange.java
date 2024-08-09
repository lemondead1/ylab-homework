package com.lemondead1.carshopservice.util;

import java.time.Instant;
import java.util.function.Predicate;

/**
 * Inclusive range of timestamps.
 */
public record DateRange(Instant min, Instant max) implements Predicate<Instant> {
  public static final DateRange ALL = new DateRange(Instant.MIN, Instant.MAX);

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
