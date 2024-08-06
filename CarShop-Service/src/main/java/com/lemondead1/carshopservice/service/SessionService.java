package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

public class SessionService {
  private final UserService userService;
  @Setter
  @Getter
  private int currentUserId;

  public SessionService(UserService userService) {
    this.userService = userService;
  }

  public UserRole getCurrentUserRole() {
    return currentUserId == 0 ? UserRole.ANONYMOUS : userService.getUserRole(currentUserId);
  }
}
