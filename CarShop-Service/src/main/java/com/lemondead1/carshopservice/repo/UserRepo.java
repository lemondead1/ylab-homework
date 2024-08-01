package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;

import java.util.*;

public class UserRepo {
  public record User(int id, String username, String password, UserRole role) { }

  private final Map<Integer, User> map = new HashMap<>();
  private final Map<String, User> usernameMap = new HashMap<>();
  private int lastId;

  public int create(String username, String password, UserRole role) {
    if (usernameMap.containsKey(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    lastId++;
    var newUser = new User(lastId, username, password, role);
    usernameMap.put(username, newUser);
    map.put(lastId, newUser);
    return lastId;
  }

  public void edit(int id, String newUsername, String newPassword, UserRole newRole) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    if (usernameMap.containsKey(newUsername)) {
      throw new UserAlreadyExistsException("Username '" + newUsername + "' is already taken.");
    }
    var newUser = new User(id, newUsername, newPassword, newRole);
    var old = map.put(id, newUser);
    usernameMap.remove(Objects.requireNonNull(old).username);
    usernameMap.put(newUsername, newUser);
  }

  public User delete(int id) {
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }
    usernameMap.remove(old.username);
    return old;
  }

  public boolean existsUsername(String username) {
    return usernameMap.containsKey(username);
  }

  public User findByUsername(String username) {
    if (!usernameMap.containsKey(username)) {
      throw new RowNotFoundException();
    }
    return usernameMap.get(username);
  }

  public User findById(int id) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    return map.get(id);
  }
}
