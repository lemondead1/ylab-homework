package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.enums.UserRole;

public class SessionService {
  private final UserService userService;
  private int currentUserId;

  public SessionService(UserService userService) {
    this.userService = userService;
  }

  public int getCurrentUserId() {
    return currentUserId;
  }

  public void setCurrentUserId(int currentUserId) {
    this.currentUserId = currentUserId;
  }

  public UserRole getCurrentUserRole() {
    return currentUserId == 0 ? UserRole.ANONYMOUS : userService.getUserRole(currentUserId);
  }
}
