package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.util.Util;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HasIdEnumSetConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    var type = (ParameterizedType) context.getParameter().getParameterizedType();
    var enumType = (Class<?>) type.getActualTypeArguments()[0];
    var values = HasIdEnumConverter.getValues(enumType);
    if ("all".equalsIgnoreCase((String) source)) {
      return Set.of(values);
    }
    var map = Util.createIdToValueMap(values);
    return Arrays.stream(((String) source).split(" *, *")).map(map::get).collect(Collectors.toSet());
  }
}
