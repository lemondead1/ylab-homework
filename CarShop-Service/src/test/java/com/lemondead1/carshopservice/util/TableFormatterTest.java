package com.lemondead1.carshopservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TableFormatterTest {
  private static final String expected = """
      column_1  |column_2 |longer_column_1|even_longer_column_2
      ----------+---------+---------------+--------------------
      abcdefghij|something|3              |last_column        \s
      abacaba   |sth      |5              |lll                \s""";

  @Test
  void testTableFormatter() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    table.addRow("abcdefghij", "something", 3, "last_column");
    table.addRow("abacaba", "sth", 5, "lll");
    assertThat(table.format(false)).isEqualTo(expected);
  }

  @Test
  void testTableFormatterWithRowCount() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    table.addRow("abcdefghij", "something", 3, "last_column");
    table.addRow("abacaba", "sth", 5, "lll");
    assertThat(table.format(true)).isEqualTo(expected + "\nRow count: 2");
  }

  @Test
  void addRowThrowsOnWrongRowLength() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    assertThatThrownBy(() -> table.addRow(1, 2, 3)).isInstanceOf(IllegalArgumentException.class);
  }
}
