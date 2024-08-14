package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.parsing.IdParser;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class HasIdEnumConverter implements ArgumentConverter {
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return IdParser.of((Class) context.getParameter().getType()).parse((String) source);
  }
}
