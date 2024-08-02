package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

import java.util.*;
import java.util.stream.Collectors;

public class EnumParser<T extends Enum<T> & HasId> implements Parser<T> {

  public static <E extends Enum<E> & HasId> EnumParser<E> of(Class<E> enumType) {
    return new EnumParser<>(enumType, Arrays.asList(enumType.getEnumConstants()));
  }

  public static <E extends Enum<E> & HasId> EnumParser<E> of(Class<E> enumType, E... allowedValues) {
    return new EnumParser<>(enumType, Arrays.asList(allowedValues));
  }

  private final Map<String, T> map = new LinkedHashMap<>();

  private EnumParser(Class<T> enumClass, Collection<T> allowedValues) {
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
