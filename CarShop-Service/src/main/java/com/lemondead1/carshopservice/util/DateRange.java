package com.lemondead1.carshopservice.util;

import java.time.Instant;
import java.util.function.Predicate;

/**
 * Inclusive range of timestamps.
 */
public class DateRange implements Predicate<Instant> {
  public static final DateRange ALL = new DateRange(Instant.MIN, Instant.MAX);

  private final Instant min;
  private final Instant max;

  public DateRange(Instant min, Instant max) {
    if (min.isAfter(max)) {
      throw new IllegalArgumentException(min + " is after " + max);
    }

    this.min = min;
    this.max = max;
  }


  @Override
  public boolean test(Instant instant) {
    return !min.isAfter(instant) && !instant.isAfter(max);
  }
}
