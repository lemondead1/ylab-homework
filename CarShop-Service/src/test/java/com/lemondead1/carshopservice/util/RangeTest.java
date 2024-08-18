package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RangeTest {
  @Test
  @DisplayName("Constructor throws when min is greater than max.")
  public void constructorThrowsWhenMinGreaterThanMax() {
    assertThatThrownBy(() -> new Range<>(2, 1)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest(name = "new Range({1}, {2}).test({0}) returns {3}.")
  @DisplayName("Testing test")
  @CsvSource(
      value = {
          "10,   null, 0,    false",
          "0,    null, 0,    true",
          "2,    0,    5,    true",
          "10,   0,    5,    false",
          "10,   0,    null, true",
          "1000, null, null, true",
          "1000, null, 999,  false"
      },
      nullValues = "null"
  )
  public void testTest(Integer value, Integer min, Integer max, boolean expected) {
    assertThat(new Range<>(min, max).test(value)).isEqualTo(expected);
  }
}
