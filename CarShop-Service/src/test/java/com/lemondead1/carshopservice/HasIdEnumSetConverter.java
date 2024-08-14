package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.parsing.IdListParser;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Set;

public class HasIdEnumSetConverter implements ArgumentConverter {
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    var type = (ParameterizedType) context.getParameter().getParameterizedType();
    var enumType = (Class) type.getActualTypeArguments()[0];

    return source.equals("ALL") ? Set.of(enumType.getEnumConstants())
                                : Set.copyOf(IdListParser.of(enumType).parse((String) source));
  }
}
