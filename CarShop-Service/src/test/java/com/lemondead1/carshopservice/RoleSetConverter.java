package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.parsing.IdListParser;
import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Set;

public class RoleSetConverter implements ArgumentConverter {
  @Override
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    return source.equals("ALL") ? Set.copyOf(UserRole.AUTHORIZED)
                                : Set.copyOf(IdListParser.of(UserRole.values()).parse((String) source));
  }
}
