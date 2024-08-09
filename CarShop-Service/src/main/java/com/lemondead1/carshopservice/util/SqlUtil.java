package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.cli.parsing.HasId;

import java.util.Set;
import java.util.stream.Collectors;

public class SqlUtil {
  public static String serializeSet(Set<? extends HasId> items) {
    return items.stream().map(HasId::getId).map(id -> "'" + id + "'").collect(Collectors.joining(", "));
  }

  public static String serializeBooleans(Set<Boolean> items) {
    return items.stream().map(b -> Boolean.toString(b)).collect(Collectors.joining(","));
  }
}
