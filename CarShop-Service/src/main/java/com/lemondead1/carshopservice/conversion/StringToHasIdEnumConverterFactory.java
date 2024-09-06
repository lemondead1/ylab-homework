package com.lemondead1.carshopservice.conversion;

import com.lemondead1.carshopservice.util.HasId;
import com.lemondead1.carshopservice.util.Util;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StringToHasIdEnumConverterFactory implements ConverterFactory<String, HasId> {
  @Override
  public <T extends HasId> Converter<String, T> getConverter(Class<T> targetType) {
    if (targetType.getEnumConstants() == null) {
      throw new IllegalArgumentException(targetType + " is not an enum.");
    }
    Map<String, T> idToValueMap = Util.createIdToValueMap(targetType.getEnumConstants());
    return source -> {
      T t = idToValueMap.get(source);
      if (t == null) {
        throw new IllegalArgumentException("Illegal value: '" + source + "'.");
      }
      return t;
    };
  }
}
