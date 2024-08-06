package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IdListParserTest {
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

  IdListParser<HasIdImpl> parser = IdListParser.of(HasIdImpl.class);

  @ParameterizedTest
  @CsvSource({
      "'0', 'id_1'",
      "'1', 'id_2  '",
      "'2', 'ID_thREE'",
      "'4', 'five-ID'",
      "'4,3,0', 'five-ID,  FOUR   id_1'",
      "'', '      '"
  })
  void parseReturnsEnumArray(String ordinalsString, String testString) {
    Integer[] ordinals = ordinalsString.isEmpty() ? new Integer[0]
                                                  : Arrays.stream(ordinalsString.split(","))
                                                          .map(Integer::parseInt)
                                                          .toArray(Integer[]::new);
    assertThat(parser.parse(testString)).map(Enum::ordinal).containsExactly(ordinals);
  }

  @ParameterizedTest
  @ValueSource(strings = { "a", "2", "ELEM_5   " })
  void parseThrowsValidationExceptionOnInvalidId(String testValue) {
    var msg = "Invalid value '" + testValue.strip() + "'. Valid values: 'id_1', 'id_2', 'id_three', 'four', 'five-id'.";
    assertThatThrownBy(() -> parser.parse(testValue)).isInstanceOf(ParsingException.class).message().isEqualTo(msg);
  }

  @ParameterizedTest
  @ValueSource(strings = { "a , ,", ", ,,", "     ,", "abc dfg, " })
  void parseThrowsValidationExceptionOnInvalidList(String testValue) {
    assertThatThrownBy(() -> parser.parse(testValue)).isInstanceOf(ParsingException.class);
  }
}
