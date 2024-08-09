package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class StringUtilTest {
  @Test
  void testFormat() {
    var template = "Lorem Ipsum is {} dummy text of the printing and {} industry.";
    var expected = "Lorem Ipsum is simply dummy text of the printing and {} industry.";

    assertThat(StringUtil.format(template, "simply", "{}")).isEqualTo(expected);

  }
}
