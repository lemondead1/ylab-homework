package com.lemondead1.carshopservice.util;

public class StringUtil {
  public static String format(String template, Object... substitutions) {
    var builder = new StringBuilder();

    int index = 0;

    var startedPlaceholder = false;

    for (int i = 0; i < template.length(); i++) {
      var ch = template.charAt(i);

      if (startedPlaceholder && ch == '}') {
        builder.append(substitutions[index++]);
        startedPlaceholder = false;
        continue;
      }

      if (startedPlaceholder) {
        builder.append('{');
      }

      if (ch == '{') {
        startedPlaceholder = true;
      } else {
        builder.append(ch);
        startedPlaceholder = false;
      }
    }

    return builder.toString();
  }
}
