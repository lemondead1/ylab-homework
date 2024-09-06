package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RangeConverter implements ArgumentConverter {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M[.uuuu][.uu][ HH:mm:ss]");

  private Comparable<?> parseValue(String string, Class<?> type, boolean start) {
    if (type.isAssignableFrom(Integer.class)) {
      return Integer.parseInt(string);
    } else if (type.isAssignableFrom(Instant.class)) {
      var best = formatter.parseBest(string, LocalDateTime::from, LocalDate::from);
      if (best instanceof LocalDate date) {
        return start ? date.atTime(0, 0, 0, 0).atZone(ZoneId.of("UTC")).toInstant()
                     : date.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.of("UTC")).toInstant();
      } else {
        return ((LocalDateTime) best).atZone(ZoneId.of("UTC")).toInstant();
      }
    } else {
      throw new IllegalArgumentException("Unsupported type " + type);
    }
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
    if ("ALL".equalsIgnoreCase((String) source)) {
      return Range.all();
    }

    var parameterizedType = context.getParameter().getParameterizedType();
    var parameterType = (Class<?>) ((ParameterizedType) parameterizedType).getActualTypeArguments()[0];

    var string = (String) source;
    var split = string.split(" *- *");
    return switch (split.length) {
      case 1 -> new Range(parseValue(split[0], parameterType, true), parseValue(split[0], parameterType, false));
      case 2 -> new Range(parseValue(split[0], parameterType, true), parseValue(split[1], parameterType, false));
      default -> throw new IllegalArgumentException();
    };
  }
}
