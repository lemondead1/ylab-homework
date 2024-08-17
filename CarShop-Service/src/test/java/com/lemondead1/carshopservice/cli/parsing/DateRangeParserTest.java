package com.lemondead1.carshopservice.cli.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DateRangeParserTest {
  private final DateRangeParser parser = new DateRangeParser(() -> ZoneId.of("UTC"));

  @ParameterizedTest
  @CsvSource({
      "153419443, 11.11.1974",
      "153446399, 11.11.1974",
      "153360000, 11.11.1974",
      "-168167710, 2.9.1964 14:44:50",
      "1100316030, 10.11.2004 - 20.11.2004",
      "1100044800, 10.11.2004 - 20.11.2004",
      "1100984399, 10.11.04 - 20.11.2004",
      "1329213312, 14.2.12 05:22:22 - 13.02.2020"
  })
  void dateRangeParserTestReturnsTrueWhenInRange(long timestamp, String range) {
    assertThat(parser.parse(range).test(Instant.ofEpochSecond(timestamp))).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
      "153359999, 11.11.1974",
      "153446400, 11.11.1974",
      "942325473, 11.11.1999 13:04:32"
  })
  void dateRangeParserTestReturnFalseWhenOutOfRange(long timestamp, String range) {
    assertThat(parser.parse(range).test(Instant.ofEpochSecond(timestamp))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "10.12",
      "10.10.2020 - 10.10.2019",
      "1.2.3 - 4.5.6 - 7.8.9"
  })
  void dateRangeParserTestThrowsParsingExceptionWhenIllegalRange(String range) {
    assertThatThrownBy(() -> parser.parse(range)).isInstanceOf(ParsingException.class);
  }
}
