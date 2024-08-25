package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface UserService {
  User findById(int userId);

  List<User> lookupUsers(String username,
                         Collection<UserRole> roles,
                         String phoneNumber,
                         String email,
                         Range<Integer> purchases,
                         UserSorting sorting);

  User createUser(String username, String phoneNumber, String email, String password, UserRole role);

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
   */
  void deleteUser(int userId);

  /**
   * Deletes the user and related orders.
   */
  void deleteUserCascading(int userId);
}
