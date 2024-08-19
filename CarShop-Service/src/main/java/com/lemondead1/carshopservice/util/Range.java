package com.lemondead1.carshopservice.util;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * An inclusive range. Nulls are assumed to be infinite values.
 */
public record Range<T extends Comparable<T>>(@Nullable T min, @Nullable T max) implements Predicate<T> {
  private static final Range<?> all = new Range<>(null, null);

  @SuppressWarnings("unchecked")
  public static <T extends Comparable<T>> Range<T> all() {
    return (Range<T>) all;
  }

  public Range {
    if (min != null && max != null && min.compareTo(max) > 0) {
      throw new IllegalArgumentException(min + " is greater than " + max);
    }
  }

  @Override
  public boolean test(T t) {
    return (min == null || min.compareTo(t) <= 0) && (max == null || t.compareTo(max) <= 0);
  }
}
