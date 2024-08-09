package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePasswordException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionService {
  private static final User anonymous = new User(0, "anonymous", "+71234567890", "test@example.com",
                                                 "password", UserRole.ANONYMOUS, 0);

  private final UserService users;
  private final EventService events;
  private int currentUserId;

  public User getCurrentUser() {
    if (currentUserId == 0) {
      return anonymous;
    }
    try {
      return users.findById(currentUserId);
    } catch (RowNotFoundException e) {
      currentUserId = 0;
      return anonymous;
    }
  }

  public void login(String username, String password) {
    User user;
    try {
      user = users.findByUsername(username);
    } catch (RowNotFoundException e) {
      throw new WrongUsernamePasswordException("Wrong username or password.");
    }
    if (!user.password().equals(password)) {
      throw new WrongUsernamePasswordException("Wrong username or password.");
    }
    currentUserId = user.id();
    events.onUserLoggedIn(user.id());
  }

  public void logout() {
    currentUserId = 0;
  }
}
