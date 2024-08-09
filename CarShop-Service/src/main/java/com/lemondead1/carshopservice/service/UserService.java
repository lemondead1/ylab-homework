package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.util.IntRange;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
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

  public User signUserUp(String username, String phoneNumber, String email, String password) {
    var user = users.create(username, phoneNumber, email, password, UserRole.CLIENT);
    events.onUserSignedUp(user);
    return user;
  }

  public User findById(int id) {
    return users.findById(id);
  }

  public User findByUsername(String username) {
    return users.findByUsername(username);
  }

  public List<User> lookupUsers(String username, Collection<UserRole> roles, String phoneNumber, String email,
                                IntRange purchases, UserSorting sorting) {
    return users.lookup(username, EnumSet.copyOf(roles), phoneNumber, email, purchases, sorting);
  }

  public User createUser(int creatorId, String username, String phoneNumber, String email, String password,
                         UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role anonymous is not allowed");
    }
    var user = users.create(username, phoneNumber, email, password, role);
    events.onUserCreated(creatorId, user);
    return user;
  }

  public User editUser(int editorId, int id,
                       @Nullable String username,
                       @Nullable String phoneNumber,
                       @Nullable String email,
                       @Nullable String password,
                       @Nullable UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role anonymous is not allowed");
    }
    var oldUser = users.findById(id);
    var newUser = users.edit(id, username, phoneNumber, email, password, role);
    events.onUserEdited(editorId, oldUser, newUser);
    return newUser;
  }

  public User deleteUser(int deleterId, int id) {
    var old = users.delete(id);
    events.onUserDeleted(deleterId, id);
    return old;
  }
}
