package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdListParser<T extends HasId> implements Parser<List<T>> {
  public static <E extends Enum<E> & HasId> IdListParser<E> of(Class<E> enumType) {
    return of(enumType.getEnumConstants());
  }

  @SafeVarargs
  public static <E extends HasId> IdListParser<E> of(E... allowedValues) {
    return new IdListParser<>(allowedValues);
  }

  private final Map<String, T> map;

  private IdListParser(T[] allowedValues) {
    map = Util.createIdToValueMap(allowedValues);
  }

  @Override
  public List<T> parse(String string) {
    if (string.isBlank()) {
      return List.of();
    }

    List<T> result = new ArrayList<>();

    var split = string.split("( +)|( *, *)");

    if (split.length == 0) {
      throw new ParsingException("Invalid list '" + string + "'.");
    }

    for (var id : split) {
      var prepared = id.toLowerCase().strip();
      if (!map.containsKey(prepared)) {
        var valuesString = map.keySet().stream().map(k -> "'" + k + "'")
                              .collect(Collectors.joining(", "));
        throw new ParsingException("Invalid value '" + id + "'. Valid values: " + valuesString + ".");
      }
      result.add(map.get(prepared));
    }

    return result;
  }
}
