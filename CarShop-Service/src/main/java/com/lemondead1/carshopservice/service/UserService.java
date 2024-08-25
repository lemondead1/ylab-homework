package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface UserService {
  @Transactional
  User findById(int userId);

  @Transactional
  List<User> lookupUsers(String username,
                         Collection<UserRole> roles,
                         String phoneNumber,
                         String email,
                         Range<Integer> purchases,
                         UserSorting sorting);

  @Transactional
  @Audited(EventType.USER_CREATED)
  User createUser(@Audited.Param("username") String username,
                  @Audited.Param("phone_number") String phoneNumber,
                  @Audited.Param("email") String email,
                  String password,
                  @Audited.Param("role") UserRole role);

  @Transactional
  @Audited(EventType.USER_MODIFIED)
  User editUser(@Audited.Param("edited_user_id") int userId,
                @Audited.Param("new_username") @Nullable String username,
                @Audited.Param("new_phone_number") @Nullable String phoneNumber,
                @Audited.Param("new_email") @Nullable String email,
                @Audited.PresenceCheck("password_changed") @Nullable String password,
                @Audited.Param("new_role") @Nullable UserRole role);

  /**
   * Deletes the user by id.
   *
   * @throws CascadingException if there exists an order referencing this user
   */
  @Transactional
  @Audited(EventType.USER_DELETED)
  void deleteUser(@Audited.Param("deleted_user_id") int userId);

  /**
   * Deletes the user and related orders.
   */
  @Transactional
  @Audited(EventType.USER_DELETED)
  void deleteUserCascading(@Audited.Param("deleted_user_id") int userId);
}
