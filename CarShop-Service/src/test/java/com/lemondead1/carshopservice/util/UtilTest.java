package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UtilTest {
  @Test
  @DisplayName("coalesce returns first argument if it is not null.")
  void coalesceReturnsFirstIfNonnull() {
    var first = new Object();
    var second = new Object();

    assertThat(Util.coalesce(first, second)).isEqualTo(first);
  }

  @Test
  @DisplayName("coalesce returns second argument if the first one is null.")
  void coalesceReturnsSecondIfFirstNull() {
    var second = new Object();

    assertThat(Util.coalesce(null, second)).isEqualTo(second);
  }

  @Test
  @DisplayName("coalesce throws if both arguments are null.")
  @SuppressWarnings("all")
  void coalesceThrowsIfBothArgumentsAreNull() {
    assertThatThrownBy(() -> Util.coalesce(null, null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("format replaces '{}' with a substitution.")
  void testFormat() {
    var template = "Lorem Ipsum is {{} dummy text of the printing and {} industry.";
    var expected = "Lorem Ipsum is {simply dummy text of the printing and {} industry.";

    assertThat(Util.format(template, "simply", "{}")).isEqualTo(expected);
  }
}
