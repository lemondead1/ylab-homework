package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import lombok.Getter;

import java.time.Instant;

public abstract class UserEvent extends Event {
  public UserEvent(Instant timestamp, int userId) {
    super(timestamp, userId);
  }

  public static class Login extends UserEvent {
    public Login(Instant timestamp, int userId) {
      super(timestamp, userId);
    }

    @Override
    public EventType getType() {
      return EventType.USER_LOGGED_IN;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId());
    }
  }

  @Getter
  public static class SignUp extends UserEvent {
    private final String username;

    public SignUp(Instant timestamp, int userId, String username) {
      super(timestamp, userId);
      this.username = username;
    }

    @Override
    public EventType getType() {
      return EventType.USER_SIGNED_UP;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "username": %s}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId());
    }
  }
}
