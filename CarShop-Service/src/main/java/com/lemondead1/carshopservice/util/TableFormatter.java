package com.lemondead1.carshopservice.util;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {
  private final String[] columnsNames;
  private final List<String[]> rows = new ArrayList<>();
  private final int[] maxColWidths;

  public TableFormatter(String... columnsNames) {
    this.columnsNames = columnsNames;
    maxColWidths = new int[columnsNames.length];
    for (int i = 0; i < columnsNames.length; i++) {
      maxColWidths[i] = columnsNames[i].length();
    }
  }

  public void addRow(Object... row) {
    if (row.length != columnsNames.length) {
      throw new IllegalArgumentException("Column count must match row length.");
    }
    var rowSerialized = new String[row.length];
    for (int i = 0; i < row.length; i++) {
      rowSerialized[i] = row[i].toString();
      maxColWidths[i] = Math.max(maxColWidths[i], rowSerialized[i].length());
    }
    rows.add(rowSerialized);
  }

  public String format() {
    int cols = columnsNames.length;

    StringBuilder builder = new StringBuilder();

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

    for (int j = 0; j < rows.size(); j++) {
      if (j > 0) {
        builder.append('\n');
      }
      var row = rows.get(j);
      for (int i = 0; i < cols; i++) {
        if (i > 0) {
          builder.append('|');
        }
        builder.append(row[i]);
        builder.append(" ".repeat(maxColWidths[i] - row[i].length()));
      }
    }

    return builder.toString();
  }
}
