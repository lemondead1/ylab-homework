package com.lemondead1.carshopservice.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lemondead1.carshopservice.enums.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HasIdModule extends SimpleModule {
  {
    registerForType(UserRole.class, UserRole.ALL.toArray(new UserRole[0]));
    registerForEnum(CarSorting.class);
    registerForEnum(EventSorting.class);
    registerForEnum(EventType.class);
    registerForEnum(OrderKind.class);
    registerForEnum(OrderSorting.class);
    registerForEnum(OrderState.class);
    registerForEnum(UserSorting.class);
  }

  @SafeVarargs
  private <T extends HasId> void registerForType(Class<T> type, T... values) {
    addDeserializer(type, new HasIdDeserializer<>(type, values));
    addSerializer(new HasIdSerializer<>(type));
  }

  private <T extends Enum<T> & HasId> void registerForEnum(Class<T> type) {
    registerForType(type, type.getEnumConstants());
  }

  @RequiredArgsConstructor
  private static class HasIdDeserializer<T extends HasId> extends JsonDeserializer<T> {
    private final Class<T> clazz;
    private final Map<String, T> idMap;

    private HasIdDeserializer(Class<T> clazz, T[] values) {
      this.clazz = clazz;
      idMap = Arrays.stream(values).collect(Collectors.toMap(HasId::getId, Function.identity()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      var id = p.getValueAsString();
      var found = idMap.get(id);
      if (found == null) {
        return (T) ctxt.handleWeirdStringValue(clazz, id, "Wrong value: '%s'.", id);
      }
      return found;
    }
  }

  @RequiredArgsConstructor
  private static class HasIdSerializer<T extends HasId> extends JsonSerializer<T> {
    private final Class<T> clazz;

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeString(value.getId());
    }

    @Override
    public Class<T> handledType() {
      return clazz;
    }
  }
}
