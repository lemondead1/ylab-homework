package com.lemondead1.carshopservice;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HasIdEnumSetConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    var type = (ParameterizedType) context.getParameter().getParameterizedType();
    var enumType = (Class<?>) type.getActualTypeArguments()[0];

    if ("all".equalsIgnoreCase((String) source)) {
      try {
        return Set.of((Object[]) enumType.getMethod("values").invoke(null));
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return Arrays.stream(((String) source).split(" *, *"))
                 .map(id -> HasIdEnumConverter.convert(id, enumType))
                 .collect(Collectors.toSet());
  }
}
