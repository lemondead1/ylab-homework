package com.lemondead1.carshopservice.util;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class DateRange implements Predicate<Instant> {
  public static final DateRange ALL = new DateRange(Instant.MIN, Instant.MAX);

  private final Instant min;
  private final Instant max;

  @Override
  public boolean test(Instant instant) {
    return !min.isAfter(instant) && !instant.isAfter(max);
  }
}
