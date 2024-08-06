package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * The parent class for all events.
 */
@Getter
public abstract class Event {
  private final Instant timestamp;

  /**
   * The user that triggered the event
   */
  private final int userId;

  Event(Instant timestamp, int userId) {
    Objects.requireNonNull(timestamp);
    this.timestamp = timestamp;
    this.userId = userId;
  }

  public abstract EventType getType();

  /**
   * Serializes itself to a single line of JSON
   * @return JSON serialization
   */
  public abstract String serialize();
}
