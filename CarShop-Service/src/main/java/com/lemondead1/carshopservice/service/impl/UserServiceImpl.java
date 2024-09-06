package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.audit.annotations.Audited;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.logging.annotations.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Timed
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepo users;
  private final OrderRepo orders;

  @Transactional
  @Override
  public User findById(int userId) {
    return users.findById(userId);
  }

  @Transactional
  @Override
  public List<User> lookupUsers(String username,
                                Collection<UserRole> roles,
                                String phoneNumber,
                                String email,
                                Range<Integer> purchases,
                                UserSorting sorting) {
    return users.lookup(username, new HashSet<>(roles), phoneNumber, email, purchases, sorting);
  }

  @Transactional
  @Audited("user_created")
  @Override
  public User createUser(@Audited.Param("username") String username,
                         @Audited.Param("phone_number") String phoneNumber,
                         @Audited.Param("email") String email,
                         String password,
                         @Audited.Param("role") UserRole role) {
    if (users.existsUsername(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    return users.create(username, phoneNumber, email, password, role);
  }

  @Transactional
  @Audited("user_edited")
  @Override
  public User editUser(@Audited.Param("edited_user_id") int userId,
                       @Audited.Param("new_username") @Nullable String username,
                       @Audited.Param("new_phone_number") @Nullable String phoneNumber,
                       @Audited.Param("new_email") @Nullable String email,
                       @Audited.PresenceCheck("password_changed") @Nullable String password,
                       @Audited.Param("new_role") @Nullable UserRole role) {
    return users.edit(userId, username, phoneNumber, email, password, role);
  }

  @Transactional
  @Audited("user_deleted")
  @Override
  public void deleteUser(@Audited.Param("deleted_user_id") int userId) {
    if (orders.doAnyOrdersExistFor(userId)) {
      throw new CascadingException(orders.countClientOrders(userId) + " order(s) reference this user.");
    }

    users.delete(userId);
  }

  @Transactional
  @Audited("user_deleted")
  @Override
  public void deleteUserCascading(@Audited.Param("deleted_user_id") int userId) {
    orders.deleteClientOrders(userId);
    users.delete(userId);
  }
}
