package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TableFormatterTest {
  private static final String expected = """
      column_1  |column_2 |longer_column_1|even_longer_column_2
      ----------+---------+---------------+--------------------
      abcdefghij|something|3              |one-line           \s
                |         |               |two-line           \s
                |         |               |three-line         \s
      abacaba   |sth      |5              |lll                \s
      multiline |         |               |                   \s""";

  @Test
  @DisplayName("format(false) returns the correct table.")
  void testTableFormatter() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    table.addRow("abcdefghij", "something", 3, "one-line\ntwo-line\nthree-line");
    table.addRow("abacaba\nmultiline", "sth", 5, "lll");
    assertThat(table.format(false)).isEqualTo(expected);
  }

  @Test
  @DisplayName("format(true) returns a table with a row count line.")
  void testTableFormatterWithRowCount() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    table.addRow("abcdefghij", "something", 3, "one-line\ntwo-line\nthree-line");
    table.addRow("abacaba\nmultiline", "sth", 5, "lll");
    assertThat(table.format(true)).isEqualTo(expected + "\nRow count: 2");
  }

  @Test
  @DisplayName("addRow throws when row length does not match header length.")
  void addRowThrowsOnWrongRowLength() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    assertThatThrownBy(() -> table.addRow(1, 2, 3)).isInstanceOf(IllegalArgumentException.class);
  }
}
