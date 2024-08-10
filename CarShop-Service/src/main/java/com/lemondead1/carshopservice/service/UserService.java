package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.util.IntRange;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class UserService {
  private final UserRepo users;
  private final OrderRepo orders;
  private final EventService events;

  public boolean checkUsernameFree(String username) {
    return !users.existsUsername(username);
  }

  public User signUserUp(String username, String phoneNumber, String email, String password) {
    if (users.existsUsername(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    var user = users.create(username, phoneNumber, email, password, UserRole.CLIENT);
    events.onUserSignedUp(user);
    return user;
  }

  public User findById(int userId) {
    return users.findById(userId);
  }

  public User findByUsername(String username) {
    return users.findByUsername(username);
  }

  public List<User> lookupUsers(String username, Collection<UserRole> roles, String phoneNumber, String email,
                                IntRange purchases, UserSorting sorting) {
    return users.lookup(username, EnumSet.copyOf(roles), phoneNumber, email, purchases, sorting);
  }

  public User createUser(int creatorId, String username, String phoneNumber,
                         String email, String password, UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role 'anonymous' is not allowed");
    }
    if (users.existsUsername(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    var user = users.create(username, phoneNumber, email, password, role);
    events.onUserCreated(creatorId, user);
    return user;
  }

  public User editUser(int userId,
                       int userIdToEdit,
                       @Nullable String username,
                       @Nullable String phoneNumber,
                       @Nullable String email,
                       @Nullable String password,
                       @Nullable UserRole role) {
    if (role == UserRole.ANONYMOUS) {
      throw new IllegalArgumentException("Role anonymous is not allowed");
    }
    var oldUser = users.findById(userIdToEdit);
    var newUser = users.edit(userIdToEdit, username, phoneNumber, email, password, role);
    events.onUserEdited(userId, oldUser, newUser);
    return newUser;
  }

  public void deleteUser(int userId, int userIdToDelete) {
    if (orders.doAnyOrdersExistFor(userIdToDelete)) {
      throw new CascadingException(orders.countClientOrders(userIdToDelete) + " order(s) reference this user.");
    }

    users.delete(userIdToDelete);
    events.onUserDeleted(userId, userIdToDelete);
  }

  public void deleteUserCascading(int userId, int userIdToDelete) {
    orders.deleteClientOrders(userIdToDelete);

    users.delete(userIdToDelete);
    events.onUserDeleted(userId, userIdToDelete);
  }
}
