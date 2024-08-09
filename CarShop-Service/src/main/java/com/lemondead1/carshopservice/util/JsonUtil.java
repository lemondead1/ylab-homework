package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.cli.parsing.HasId;

public class JsonUtil {
  public static String escapeCharacters(String raw) {
    var builder = new StringBuilder();
    for (int i = 0; i < raw.length(); i++) {
      var ch = raw.charAt(i);
      switch (ch) {
        case '"' -> builder.append("\\\"");
        case '\\' -> builder.append("\\\\");
        case '/' -> builder.append("\\/");
        case '\b' -> builder.append("\\b");
        case '\f' -> builder.append("\\f");
        case '\n' -> builder.append("\\n");
        case '\r' -> builder.append("\\r");
        case '\t' -> builder.append("\\t");
        case '\0' -> builder.append("\\u0000");
        default -> builder.append(ch);
      }
    }
    return builder.toString();
  }

  public static Builder jsonBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final StringBuilder builder = new StringBuilder("{");

    public Builder append(String key, Object value) {
      builder.append('"').append(key).append("\": ");

      if (value instanceof Number) {
        builder.append(value);
      } else if (value instanceof HasId) {
        builder.append('"').append(((HasId) value).getId()).append('"');
      } else {
        builder.append('"').append(escapeCharacters(value.toString())).append('"');
      }

      builder.append(", ");

      return this;
    }

    public String build() {
      builder.setLength(builder.length() - 2);
      builder.append('}');
      return builder.toString();
    }
  }
}
