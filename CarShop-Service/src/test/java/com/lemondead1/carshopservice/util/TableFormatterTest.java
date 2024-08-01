package com.lemondead1.carshopservice.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TableFormatterTest {
  private static final String expected = """
      column_1  |column_2 |longer_column_1|even_longer_column_2
      ----------+---------+---------------+--------------------
      abcdefghij|something|3              |last_column        \s""";

  @Test
  void testTableFormatter() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    table.addRow("abcdefghij", "something", 3, "last_column");
    assertThat(table.format()).isEqualTo(expected);
  }

  @Test
  void addRowThrowsOnWrongRowLength() {
    var table = new TableFormatter("column_1", "column_2", "longer_column_1", "even_longer_column_2");
    assertThatThrownBy(() -> table.addRow(1, 2, 3)).isInstanceOf(IllegalArgumentException.class);
  }
}
