package com.lemondead1.carshopservice.dto.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.lemondead1.carshopservice.enums.EventType;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an existing event.
 *
 * @param data Auxiliary data. It is embedded into the object when serialized.
 */
public record EventDTO(int id,
                       Instant timestamp,
                       int userId,
                       EventType type,
                       @JsonAnyGetter Map<String, Object> data) { }
