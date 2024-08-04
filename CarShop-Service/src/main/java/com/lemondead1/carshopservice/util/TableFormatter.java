package com.lemondead1.carshopservice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Formats equal length rows into tables
 */
public class TableFormatter {
  private final String[] columnsNames;
  private final List<String[]> rows = new ArrayList<>();


  public TableFormatter(String... columnsNames) {
    this.columnsNames = columnsNames;
  }

  public void addRow(Object... row) {
    if (row.length != columnsNames.length) {
      throw new IllegalArgumentException("Column count must match row length.");
    }
    var rowSerialized = new String[row.length];
    for (int i = 0; i < row.length; i++) {
      rowSerialized[i] = row[i].toString();
    }
    rows.add(rowSerialized);
  }

  public String format(boolean printRowCount) {
    int cols = columnsNames.length;

    StringBuilder builder = new StringBuilder();

    List<String[]> actualRows = new ArrayList<>();

    for (var row : rows) {
      int currIndex = actualRows.size();

      for (int i = 0; i < cols; i++) {
        var lines = row[i].split("\n");
        for (int j = 0; j < lines.length; j++) {
          var line = lines[j];

          int actualRowIndex = currIndex + j;

          String[] actualRow;

          if (actualRowIndex < actualRows.size()) {
            actualRow = actualRows.get(actualRowIndex);
          } else {
            actualRow = new String[cols];
            Arrays.fill(actualRow, "");
            actualRows.add(actualRow);
          }

          actualRow[i] = line;
        }
      }
    }

    int[] maxColWidths = new int[cols];

    for (int i = 0; i < cols; i++) {
      maxColWidths[i] = columnsNames[i].length();
    }

    for (var row : actualRows) {
      for (int i = 0; i < cols; i++) {
        maxColWidths[i] = Math.max(maxColWidths[i], row[i].length());
      }
    }

    for (int i = 0; i < cols; i++) {
      if (i > 0) {
        builder.append('|');
      }
      builder.append(columnsNames[i]);
      builder.append(" ".repeat(maxColWidths[i] - columnsNames[i].length()));
    }

    builder.append('\n');

    for (int i = 0; i < cols; i++) {
      if (i > 0) {
        builder.append('+');
      }
      builder.append("-".repeat(maxColWidths[i]));
    }

    builder.append('\n');

    for (int j = 0; j < actualRows.size(); j++) {
      if (j > 0) {
        builder.append('\n');
      }
      var row = actualRows.get(j);
      for (int i = 0; i < cols; i++) {
        if (i > 0) {
          builder.append('|');
        }
        builder.append(row[i]);
        builder.append(" ".repeat(maxColWidths[i] - row[i].length()));
      }
    }

    if (printRowCount) {
      builder.append('\n');
      builder.append("Row count: ").append(rows.size());
    }

    return builder.toString();
  }
}
