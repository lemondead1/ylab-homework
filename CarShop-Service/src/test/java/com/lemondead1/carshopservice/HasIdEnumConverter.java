package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.InvocationTargetException;

public class HasIdEnumConverter implements ArgumentConverter {
  static HasId[] getValues(Class<?> type) {
    try {
      return (HasId[]) type.getMethod("values").invoke(null);
    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return Util.createIdToValueMap(getValues(context.getParameter().getType())).get((String) source);
  }
}
