package com.lemondead1.carshopservice.util;

import java.util.function.IntPredicate;

/**
 * Inclusive range of integers.
 */
public record IntRange(int min, int max) implements IntPredicate {
  public static final IntRange ALL = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

  public IntRange {
    if (min > max) {
      throw new IllegalArgumentException(min + " is greater than " + max);
    }
  }

  @Override
  public boolean test(int value) {
    return min <= value && value <= max;
  }
}
