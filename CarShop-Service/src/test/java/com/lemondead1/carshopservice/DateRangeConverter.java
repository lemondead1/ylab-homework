package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.parsing.DateRangeParser;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.time.ZoneId;

public class DateRangeConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return "ALL".equals(source) ? DateRange.ALL : new DateRangeParser(() -> ZoneId.of("UTC")).parse((String) source);
  }
}
