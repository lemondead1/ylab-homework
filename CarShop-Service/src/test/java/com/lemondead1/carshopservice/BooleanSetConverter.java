package com.lemondead1.carshopservice;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Set;

public class BooleanSetConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return switch ((String) source) {
      case "true" -> Set.of(true);
      case "false" -> Set.of(false);
      case "ALL" -> Set.of(true, false);
      default -> throw new IllegalStateException("Unexpected value: " + (String) source);
    };
  }
}
