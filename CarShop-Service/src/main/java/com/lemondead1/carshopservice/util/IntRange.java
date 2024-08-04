package com.lemondead1.carshopservice.util;

import java.util.function.IntPredicate;

/**
 * Inclusive range of integers.
 */
public class IntRange implements IntPredicate {
  public static IntRange ALL = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

  private final int min;
  private final int max;

  public IntRange(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException(min + " is greater than " + max);
    }

    this.min = min;
    this.max = max;
  }

  @Override
  public boolean test(int value) {
    return min <= value && value <= max;
  }
}
