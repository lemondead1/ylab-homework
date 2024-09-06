package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;

/**
 * Checks if the values is a past year.
 */
@RequiredArgsConstructor
public class PastYearValidator implements Validator<Integer> {
  public static PastYearValidator INSTANCE = new PastYearValidator(Instant::now);

  private final Supplier<Instant> nowSupplier;

  @Override
  public void validate(Integer value) {
    Instant now = nowSupplier.get();
    OffsetDateTime date = now.atOffset(ZoneOffset.UTC);
    int currentYear = date.getYear();
    if (value > currentYear) {
      throw new ValidationException("Year must be in the past.");
    }
  }
}
