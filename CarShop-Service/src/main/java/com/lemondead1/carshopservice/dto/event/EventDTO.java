package com.lemondead1.carshopservice.dto.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.lemondead1.carshopservice.enums.EventType;

import java.time.Instant;
import java.util.Map;

public record EventDTO(int id,
                       Instant timestamp,
                       int userId,
                       EventType type,
                       @JsonAnyGetter Map<String, Object> data) { }
