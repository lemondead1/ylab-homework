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

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    Event event = (Event) o;
    return userId == event.userId && timestamp.equals(event.timestamp);
  }

  @Override
  public int hashCode() {
    int result = timestamp.hashCode();
    result = 31 * result + userId;
    result = 31 * result + getType().hashCode();
    return result;
  }
}
