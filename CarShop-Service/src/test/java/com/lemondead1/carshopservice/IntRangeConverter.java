package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.parsing.IntRangeParser;
import com.lemondead1.carshopservice.util.IntRange;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class IntRangeConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return source == null ? null : IntRangeParser.INSTANCE.parse((String) source);
  }
}
