package com.lemondead1.carshopservice.cli.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IntParserTest {
  @ParameterizedTest
  @CsvSource({
      "123, 123",
      "345, 345",
      "0, 0",
      "-10, -10"
  })
  void parseParsesInt(int expected, String test) {
    assertThat(IntParser.INSTANCE.parse(test)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "abcdefg",
      "102.53"
  })
  void parseThrowsValidationException(String test) {
    assertThatThrownBy(() -> IntParser.INSTANCE.parse(test)).isInstanceOf(ParsingException.class)
                                                            .message().isEqualTo("'" + test + "' is not an integer.");
  }
}
