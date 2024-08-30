package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.DBInitializer;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ContextConfiguration(initializers = DBInitializer.class)
public class UserServiceTest {
  @Autowired
  UserRepo users;

  @Autowired
  OrderRepo orders;

  @Autowired
  UserService userService;
  
  @BeforeEach
  void beforeEach() {
    var currentRequest = new MockHttpServletRequest();
    currentRequest.setUserPrincipal(new User(1, "admin", "88005553535", "admin@ya.com", "password", UserRole.ADMIN, 0));
    RequestContextHolder.setRequestAttributes(new ServletWebRequest(currentRequest));
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
