package com.lemondead1.carshopservice.cli.validation;

public interface Validator<T> {
  void validate(T value);
}
