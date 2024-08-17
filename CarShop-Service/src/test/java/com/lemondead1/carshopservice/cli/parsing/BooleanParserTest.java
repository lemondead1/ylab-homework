package com.lemondead1.carshopservice.cli.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BooleanParserTest {
  @ParameterizedTest
  @ValueSource(strings = { "yes", "true", "y", "Y", "TRUE" })
  void anyBooleanParserReturnsTrue(String input) {
    assertThat(BooleanParser.THROW.parse(input)).isTrue();
    assertThat(BooleanParser.DEFAULT_TO_FALSE.parse(input)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = { "n", "no", "false", "False", "N" })
  void anyBooleanParserReturnsFalse(String input) {
    assertThat(BooleanParser.THROW.parse(input)).isFalse();
    assertThat(BooleanParser.DEFAULT_TO_FALSE.parse(input)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = { "hello", "yea", "yass", "nana" })
  void defaultingParserReturnsFalseAndThrowingThrows(String input) {
    assertThatThrownBy(() -> BooleanParser.THROW.parse(input)).isInstanceOf(ParsingException.class);
    assertThat(BooleanParser.DEFAULT_TO_FALSE.parse(input)).isFalse();
  }
}
