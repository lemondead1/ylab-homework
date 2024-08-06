package com.lemondead1.carshopservice.util;

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
}
