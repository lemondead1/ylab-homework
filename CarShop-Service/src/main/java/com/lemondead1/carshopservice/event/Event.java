package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;

import java.time.Instant;
import java.util.Objects;

public abstract class Event {
  private final Instant timestamp;
  private final int userId;

  Event(Instant timestamp, int userId) {
    Objects.requireNonNull(timestamp);
    this.timestamp = timestamp;
    this.userId = userId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public int getUserId() {
    return userId;
  }

  public abstract EventType getType();

  public abstract String serialize();
}
