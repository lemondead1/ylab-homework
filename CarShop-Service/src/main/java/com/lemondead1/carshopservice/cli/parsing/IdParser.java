package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;

import java.util.Map;
import java.util.stream.Collectors;

public class IdParser<T extends HasId> implements Parser<T> {
  public static <E extends Enum<E> & HasId> IdParser<E> of(Class<E> enumType) {
    return of(enumType.getEnumConstants());
  }

  @SafeVarargs
  public static <E extends HasId> IdParser<E> of(E... allowedValues) {
    return new IdParser<>(allowedValues);
  }

  private final Map<String, T> map;

  private IdParser(T[] allowedValues) {
    map = Util.createIdToValueMap(allowedValues);
  }

  @Override
  public T parse(String string) {
    var prepared = string.toLowerCase().strip();
    if (!map.containsKey(prepared)) {
      var valuesString = map.keySet().stream().map(k -> "'" + k + "'")
                            .collect(Collectors.joining(", "));
      throw new ParsingException("Invalid value '" + string + "'. Valid values: " + valuesString + ".");
    }
    return map.get(prepared);
  }
}
