package com.lemondead1.carshopservice.entity;

import com.lemondead1.carshopservice.enums.EventType;

import java.time.Instant;
import java.util.Map;

public record Event(int id, Instant timestamp, int userId, EventType type, Map<String, Object> json) { }
