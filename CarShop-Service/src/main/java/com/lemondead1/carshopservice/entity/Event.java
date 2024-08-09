package com.lemondead1.carshopservice.entity;

import com.lemondead1.carshopservice.enums.EventType;

import java.time.Instant;

public record Event(int id, Instant timestamp, int userId, EventType type, String json) { }
