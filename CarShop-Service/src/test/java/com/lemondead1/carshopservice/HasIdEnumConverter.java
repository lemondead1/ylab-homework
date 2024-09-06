package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class HasIdEnumConverter implements ArgumentConverter {
  static HasId[] getValues(Class<?> type) {
    return (HasId[]) type.getEnumConstants();
  }

  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return Util.createIdToValueMap(getValues(context.getParameter().getType())).get((String) source);
  }
}
