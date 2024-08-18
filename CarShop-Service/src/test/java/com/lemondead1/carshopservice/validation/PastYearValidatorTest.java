package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

public class PastYearValidatorTest {
  @ParameterizedTest
  @CsvSource({
      "2000, 1999-12-31T23:59:59Z, false",
      "1999, 2000-01-01T00:00:00Z, true"
  })
  public void testPastYear(int year, Instant now, boolean result) {
    if (result) {
      new PastYearValidator(() -> now).validate(year);
    } else {
      Assertions.assertThatThrownBy(() -> new PastYearValidator(() -> now).validate(year))
                .isInstanceOf(ValidationException.class);
    }
  }
}
