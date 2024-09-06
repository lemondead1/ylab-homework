package com.lemondead1.carshopservice.conversion;

import com.lemondead1.carshopservice.util.HasId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class HasIdToStringConverter implements Converter<HasId, String> {
  @Override
  public String convert(HasId value) {
    return value.getId();
  }
}
