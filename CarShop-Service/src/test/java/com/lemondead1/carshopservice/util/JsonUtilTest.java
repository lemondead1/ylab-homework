package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilTest {
  private static final String testString = """
      Lorem ipsum
      \tdolor sit\t amet,\f \b /\\ consectetur\r
      \tadipiscing elit.\\ "Nunc gravida."\0
      """;

  private static final String expected = """
      Lorem ipsum\\n\\tdolor sit\\t amet,\\f \\b \\/\\\\ consectetur\\r\\n\\tadipiscing elit.\\\\ \\"Nunc gravida.\\"\\u0000\\n""";

  @Test
  void testEscapeCharacters() {
    assertThat(JsonUtil.escapeCharacters(testString)).isEqualTo(expected);
  }
}
