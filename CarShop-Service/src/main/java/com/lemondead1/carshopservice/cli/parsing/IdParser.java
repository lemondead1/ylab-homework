package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IdParser<T extends HasId> implements Parser<T> {
  public static <E extends Enum<E> & HasId> IdParser<E> of(Class<E> enumType) {
    return of(enumType.getEnumConstants());
  }

  @SafeVarargs
  public static <E extends HasId> IdParser<E> of(E... allowedValues) {
    return new IdParser<>(Arrays.asList(allowedValues));
  }

  private final Map<String, T> map = new LinkedHashMap<>();

  private IdParser(Collection<T> allowedValues) {
    for (var constant : allowedValues) {
      map.put(constant.getId(), constant);
    }
  }

  @Override
  public T parse(String string) {
    if (!map.containsKey(string.toLowerCase())) {
      var valuesString = map.keySet().stream().map(k -> "'" + k + "'")
                            .collect(Collectors.joining(", "));
      throw new ParsingException("Invalid value '" + string + "'. Valid values: " + valuesString  + ".");
    }
    return map.get(string.toLowerCase());
  }
}
