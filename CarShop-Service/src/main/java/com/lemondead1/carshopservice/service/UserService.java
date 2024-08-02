package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePassword;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UserService {
  private final UserRepo users;
  private final EventService events;

  public UserService(UserRepo users, EventService events) {
    this.users = users;
    this.events = events;
  }

  public boolean checkUsernameFree(String username) {
    return !users.existsUsername(username);
  }

  public void signUserUp(String username, String password) {
    var user = users.create(username, password, UserRole.CLIENT);
    events.onUserSignedUp(user.id(), username);
  }

  public UserRole getUserRole(int userId) {
    return users.findById(userId).role();
  }

  public void login(String username, String password, SessionService session) {
    User user;
    try {
      user = users.findByUsername(username);
    } catch (RowNotFoundException e) {
      throw new WrongUsernamePassword("Wrong username or password.");
    }
    if (!user.password().equals(password)) {
      throw new WrongUsernamePassword("Wrong username or password.");
    }
    session.setCurrentUserId(user.id());
    events.onUserLoggedIn(user.id());
  }

  public User findById(int id) {
    return users.findById(id);
  }

  public List<User> searchUsers(@Nullable String username, @Nullable UserRole role, UserSorting sorting) {
    return users.search(username, role, sorting);
  }

  public User createUser(int creatorId, String username, String password, UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role anonymous is not allowed");
    }
    var user = users.create(username, password, role);
    events.onUserCreated(creatorId, user);
    return user;
  }

  public User editUser(int editorId, int id, @Nullable String username, @Nullable String password,
                       @Nullable UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role anonymous is not allowed");
    }
    var oldUser = users.findById(id);
    var newUser = users.edit(id).username(username).password(password).role(role).apply();
    events.onUserEdited(editorId, oldUser, newUser);
    return newUser;
  }
}
