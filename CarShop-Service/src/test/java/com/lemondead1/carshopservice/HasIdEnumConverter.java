package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class HasIdEnumConverter implements ArgumentConverter {
  static Object convert(Object source, Class<?> type) {
    try {
      var method = type.getMethod("parse", String.class);
      return method.invoke(null, source);
    } catch (NoSuchMethodException ignored) {
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    HasId[] arr = Arrays.stream(type.getEnumConstants())
                        .map(HasId.class::cast)
                        .toArray(HasId[]::new);
    return Util.createIdToValueMap(arr).get((String) source);
  }

  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return convert(source, context.getParameter().getType());
  }
}
