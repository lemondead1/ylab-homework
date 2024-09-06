package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  private static final UserRepo users = new UserRepo(TestDBConnector.DB_MANAGER);
  private static final OrderRepo orders = new OrderRepo(TestDBConnector.DB_MANAGER);

  UserService userService;

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
    userService = new UserServiceImpl(users, orders);
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @Test
  @DisplayName("createUser saves user into the repo.")
  void createUserSavesUserAndPostsEvent() {
    var user = userService.createUser("obemna", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(user.id())).isEqualTo(user);
  }

  @Test
  @DisplayName("createUser throws UserAlreadyExistsException when attempting to add a user with a taken username.")
  void createUserThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> userService.createUser("admin", "123456789", "test@x.com", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("editUser edits the user in the repo.")
  void editSavesNewUser() {
    var oldUser = userService.createUser("steve", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    var newUser = userService.editUser(oldUser.id(), "bob", "+5334342", "test@ya.com", "password", UserRole.CLIENT);
    assertThat(users.findById(oldUser.id()))
        .isEqualTo(newUser)
        .isEqualTo(new User(oldUser.id(), "bob", "+5334342", "test@ya.com", "password", UserRole.CLIENT, 0));
  }

  @Test
  @DisplayName("editUser throws a RowNotFoundException when userId is not found.")
  void editThrowsOnNonExistentUser() {
    assertThatThrownBy(() -> userService.editUser(500, "newUsername", "+5334342", null, null, UserRole.CLIENT))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("deleteUser deletes user from the repo.")
  void deleteUserDeletesUser() {
    userService.deleteUser(78);
    assertThatThrownBy(() -> users.findById(78)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("deleteUserCascade deletes the user and associated orders.")
  void deleteUserCascadeTest() {
    userService.deleteUserCascading(18);

    assertThatThrownBy(() -> users.findById(18)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(253)).isInstanceOf(NotFoundException.class);
  }
}
