package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePassword;
import com.lemondead1.carshopservice.repo.UserRepo;

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
    int id = users.create(username, password, UserRole.CLIENT);
    events.onUserSignedUp(id, username);
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
  }
}
