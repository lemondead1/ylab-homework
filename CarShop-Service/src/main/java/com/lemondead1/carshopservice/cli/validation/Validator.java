package com.lemondead1.carshopservice.cli.validation;

public interface Validator<T> {
  /**
   * Performs input checks
   *
   * @param value value to be validated
   * @throws com.lemondead1.carshopservice.exceptions.ValidationException on validation failure
   */
  void validate(T value);
}
