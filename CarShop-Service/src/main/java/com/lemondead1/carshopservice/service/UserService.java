package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface UserService {
  /**
   * Looks up a user by the given id.
   *
   * @throws NotFoundException if a user with the given id does not exist.
   */
  User findById(int userId);

  /**
   * Searches for users matching arguments.
   *
   * @param username    Username query.
   * @param roles       Role whitelist.
   * @param phoneNumber Phone number query.
   * @param email       Email query.
   * @param purchases   Purchase count range.
   * @param sorting     User sorting.
   * @return List of users matching given arguments.
   */
  List<User> lookupUsers(String username,
                         Collection<UserRole> roles,
                         String phoneNumber,
                         String email,
                         Range<Integer> purchases,
                         UserSorting sorting);

  /**
   * Creates a user according to arguments.
   *
   * @return Created user.
   * @throws UserAlreadyExistsException if the username is already taken.
   */
  User createUser(String username, String phoneNumber, String email, String password, UserRole role);

  /**
   * Patches user #{@code userId} according to arguments. Pass {@code null} to leave a field unchanged.
   *
   * @return Patched user.
   */
  User editUser(int userId,
                @Nullable String username,
                @Nullable String phoneNumber,
                @Nullable String email,
                @Nullable String password,
                @Nullable UserRole role);

  /**
   * Deletes the user by id.
   *
   * @throws CascadingException if there exists an order referencing this user
   * @throws NotFoundException if a user with the given id does not exist.
   */
  void deleteUser(int userId);

  /**
   * Deletes the user and related orders.
   *
   * @throws NotFoundException if a user with the given id does not exist.
   */
  void deleteUserCascading(int userId);
}
