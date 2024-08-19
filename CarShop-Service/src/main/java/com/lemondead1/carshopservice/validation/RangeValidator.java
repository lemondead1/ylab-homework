package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;

import javax.annotation.Nullable;

/**
 * Validates if the values is in an inclusive range.
 */
public record RangeValidator<T extends Comparable<T>>(@Nullable T min, @Nullable T max) implements Validator<T> {
  public RangeValidator {
    if (min != null && max != null && min.compareTo(max) > 0) {
      throw new IllegalArgumentException(min + " is greater than " + max);
    }
  }

  @Override
  public void validate(T value) {
    if (min != null && min.compareTo(value) > 0) {
      throw new ValidationException(value + " is smaller than " + min);
    }
    if (max != null && value.compareTo(max) > 0) {
      throw new ValidationException(value + " is greater than " + max);
    }
  }
}
