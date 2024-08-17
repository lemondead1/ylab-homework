package com.lemondead1.carshopservice.cli.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;

import java.time.LocalDate;

public enum PastYearValidator implements Validator<Integer> {
  INSTANCE;

  @Override
  public void validate(Integer value) {
    int currentYear = LocalDate.now().getYear();
    if (value > currentYear) {
      throw new ValidationException("Year must be in the past.");
    }
  }
}
