package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Validated<T> {
  @Nullable
  private final T value;

  public static <V> Validated<V> validate(@Nullable V value) {
    return new Validated<>(value);
  }

  /**
   * Validates the current value if it is present.
   * @param validator the validator for the value to be checked against
   * @return {@code this}
   */
  public Validated<T> by(Validator<? super T> validator) {
    if (value != null) {
      validator.validate(value);
    }
    return this;
  }

  /**
   * Checks if the current value is {@code null} and throws an exception in that case
   * @param message exception message
   * @return the contained value
   * @throws ValidationException if the current value is {@code null}
   */
  public T nonnull(String message) {
    if (value == null) {
      throw new ValidationException(message);
    }
    return value;
  }

  /**
   * @return the current value if it is not {@code null}, {@code defaultValue} otherwise
   */
  public T orElse(T defaultValue) {
    return value == null ? defaultValue : value;
  }

  @Nullable
  public T orNull() {
    return value;
  }
}
