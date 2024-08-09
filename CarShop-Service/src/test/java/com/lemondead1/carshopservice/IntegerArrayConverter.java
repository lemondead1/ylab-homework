package com.lemondead1.carshopservice;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Arrays;

public class IntegerArrayConverter implements ArgumentConverter {
  @Override
  public Integer[] convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return Arrays.stream(((String) source).split("( +)|( *, *)")).map(Integer::parseInt).toArray(Integer[]::new);
  }
}
