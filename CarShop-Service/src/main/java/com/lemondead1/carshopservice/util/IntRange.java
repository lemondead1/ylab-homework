package com.lemondead1.carshopservice.util;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.function.IntPredicate;

@RequiredArgsConstructor
@EqualsAndHashCode
public class IntRange implements IntPredicate {
  public static IntRange ALL = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

  private final int min;
  private final int max;

  @Override
  public boolean test(int value) {
    return min <= value && value <= max;
  }
}
