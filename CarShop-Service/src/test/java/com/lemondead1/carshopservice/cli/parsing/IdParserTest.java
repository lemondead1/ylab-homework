package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IdParserTest {
  @Getter
  @RequiredArgsConstructor
  private enum HasIdImpl implements HasId {
    ELEM_1("id_1"),
    ELEM_2("id_2"),
    ELEM_3("id_three"),
    ELEM_4("four"),
    ELEM_5("five-id");

    private final String id;
  }

  IdParser<HasIdImpl> parser;

  @BeforeEach
  void setup() {
    parser = IdParser.of(HasIdImpl.class);
  }

  @ParameterizedTest
  @CsvSource({
      "0, id_1",
      "1, 'id_2  '",
      "2, ID_thREE",
      "4, five-ID"
  })
  void parseReturnsEnumInstance(int expectedOrdinal, String testString) {
    assertThat(parser.parse(testString).ordinal()).isEqualTo(expectedOrdinal);
  }

  @ParameterizedTest
  @ValueSource(strings = { "a", "2", "ELEM_5" })
  void parseThrowsValidationExceptionOnInvalidInput(String testString) {
    var message = "Invalid value '" + testString + "'. Valid values: 'id_1', 'id_2', 'id_three', 'four', 'five-id'.";
    assertThatThrownBy(() -> parser.parse(testString)).isInstanceOf(ParsingException.class)
                                                      .message().isEqualTo(message);
  }
}
