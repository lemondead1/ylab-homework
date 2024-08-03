package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
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
          {"timestamp": "%s", "type": "%s", "user_id": %d}""";
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
          {"timestamp": "%s", "type": "%s", "user_id": %d, "username": "%s"}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getUsername());
    }
  }

  @Getter
  public static class Edited extends UserEvent {
    private final int changedUserId;
    private final String newUsername;
    private final boolean passwordChanged;
    private final UserRole newRole;

    public Edited(Instant timestamp, int userId, int changedUserId, String newUsername, boolean passwordChanged,
                  UserRole newRole) {
      super(timestamp, userId);
      this.changedUserId = changedUserId;
      this.newUsername = newUsername;
      this.passwordChanged = passwordChanged;
      this.newRole = newRole;
    }

    @Override
    public EventType getType() {
      return EventType.USER_MODIFIED;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "edited_user_id": %d, "new_username": "%s", "password_changed": %b, "new_role": "%s"}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getChangedUserId(),
                           getNewUsername(), passwordChanged, newRole.getId());
    }
  }

  @Getter
  public static class Created extends UserEvent {
    private final int createdUserId;
    private final String username;
    private final UserRole role;

    public Created(Instant timestamp, int userId, int createdUserId, String username, UserRole role) {
      super(timestamp, userId);
      this.createdUserId = createdUserId;
      this.username = username;
      this.role = role;
    }

    @Override
    public EventType getType() {
      return EventType.USER_CREATED;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "created_user_id": %d, "username": "%s", "role": "%s"}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getCreatedUserId(), getUsername(),
                           getRole().getId());
    }
  }

  @Getter
  public static class Deleted extends UserEvent {
    private final int deletedUserId;

    public Deleted(Instant timestamp, int userId, int deletedUserId) {
      super(timestamp, userId);
      this.deletedUserId = deletedUserId;
    }

    @Override
    public EventType getType() {
      return EventType.USER_DELETED;
    }

    @Override
    public String serialize() {
      String pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "deleted_user_id": %d}""";
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getDeletedUserId());
    }
  }
}
