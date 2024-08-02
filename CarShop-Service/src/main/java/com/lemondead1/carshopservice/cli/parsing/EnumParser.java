package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumParser<T extends Enum<T> & HasId> implements Parser<T> {
  public static <E extends Enum<E> & HasId> EnumParser<E> of(Class<E> enumType) {
    return new EnumParser<>(enumType);
  }

  private final Map<String, T> map = new HashMap<>();

  private EnumParser(Class<T> enumClass) {
    for (var constant : enumClass.getEnumConstants()) {
      map.put(constant.getId(), constant);
    }
  }

  @Override
  public T parse(String string) {
    if (!map.containsKey(string.toLowerCase())) {
      throw new ParsingException("Invalid value '" + string + "'. Valid values: " + String.join(", ", map.keySet()) + ".");
    }
    return map.get(string.toLowerCase());
  }
}
