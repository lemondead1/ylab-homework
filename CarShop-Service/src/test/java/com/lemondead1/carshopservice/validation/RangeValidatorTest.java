package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RangeValidatorTest {
  @Test
  @DisplayName("Constructor throws when min is greater than max.")
  public void constructorThrowsWhenMinGreaterThanMax() {
    assertThatThrownBy(() -> new RangeValidator<>(2, 1)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest(name = "new RangeValidator({1}, {2}).validate({0}) does not throw.")
  @DisplayName("Validation passing tests")
  @CsvSource(
      value = {
          "0,    null, 0",
          "2,    0,    5",
          "10,   0,    null",
          "1000, null, null"
      },
      nullValues = "null"
  )
  public void passingTest(Integer value, Integer min, Integer max) {
    new RangeValidator<>(min, max).validate(value);
  }

  @ParameterizedTest(name = "new RangeValidator({1}, {2}).validate({0}) throws.")
  @DisplayName("Validation throwing tests.")
  @CsvSource(
      value = {
          "10,   null, 0",
          "10,   0,    5",
          "1000, null, 999"
      },
      nullValues = "null"
  )
  public void nonPassingTest(Integer value, Integer min, Integer max) {
    Assertions.assertThatThrownBy(() -> new RangeValidator<>(min, max).validate(value))
              .isInstanceOf(ValidationException.class);
  }
}
