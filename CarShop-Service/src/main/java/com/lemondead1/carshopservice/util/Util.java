package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.validation.PatternValidator;
import com.lemondead1.carshopservice.validation.RangeValidator;
import com.lemondead1.carshopservice.validation.Validator;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {
  public static final Validator<Integer> POSITIVE_INT = new RangeValidator<>(1, null);
  public static final Validator<String> USERNAME = new PatternValidator("[A-Za-z0-9_\\-.]{3,20}");
  public static final Validator<String> PASSWORD = new PatternValidator("[ -~]{8,}");
  public static final Validator<String> PHONE_NUMBER = new PatternValidator("\\+?\\d{8,13}");
  public static final Validator<String> EMAIL = new PatternValidator(".+@([a-zA-Z0-9-]{1,64}.)+[a-zA-Z]{2,20}");

  public static <T> T coalesce(@Nullable T object, T defaultValue) {
    if (object != null) {
      return object;
    }
    if (defaultValue != null) {
      return defaultValue;
    }
    throw new NullPointerException("No nonnull object was found.");
  }

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

  public static String serializeSet(Set<? extends HasId> items) {
    return items.stream().map(HasId::getId).map(id -> "'" + id + "'").collect(Collectors.joining(", "));
  }

  public static String serializeBooleans(Set<Boolean> items) {
    return items.stream().map(b -> Boolean.toString(b)).collect(Collectors.joining(","));
  }

  @SafeVarargs
  public static <V extends HasId> Map<String, V> createIdToValueMap(V... array) {
    Map<String, V> map = new LinkedHashMap<>();
    for (V v : array) {
      map.put(v.getId(), v);
    }
    return map;
  }
}
