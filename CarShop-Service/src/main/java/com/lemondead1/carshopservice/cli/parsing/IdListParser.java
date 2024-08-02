package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

import java.util.*;
import java.util.stream.Collectors;

public class IdListParser<T extends HasId> implements Parser<List<T>> {
  public static <E extends Enum<E> & HasId> IdListParser<E> of(Class<E> enumType) {
    return of(enumType.getEnumConstants());
  }

  @SafeVarargs
  public static <E extends HasId> IdListParser<E> of(E... allowedValues) {
    return new IdListParser<>(Arrays.asList(allowedValues));
  }

  private final Map<String, T> map = new LinkedHashMap<>();

  private IdListParser(Collection<T> allowedValues) {
    for (var constant : allowedValues) {
      map.put(constant.getId(), constant);
    }
  }

  @Override
  public List<T> parse(String string) {
    List<T> result = new ArrayList<>();

    for (var id : string.split("[ ,] *")) {
      if (!map.containsKey(id.toLowerCase())) {
        var valuesString = map.keySet().stream().map(k -> "'" + k + "'")
                              .collect(Collectors.joining(", "));
        throw new ParsingException("Invalid value '" + id + "'. Valid values: " + valuesString + ".");
      }
      result.add(map.get(id.toLowerCase()));
    }

    return result;
  }
}
