package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.cli.parsing.HasId;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumUtil {
  public static <V extends Enum<V> & HasId> Map<String, V> createIdMap(Class<V> enumClass) {
    return Arrays.stream(enumClass.getEnumConstants()).collect(Collectors.toMap(HasId::getId, Function.identity()));
  }
}
