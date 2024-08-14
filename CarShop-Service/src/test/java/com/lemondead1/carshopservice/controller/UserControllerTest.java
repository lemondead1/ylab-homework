package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
  @Mock
  UserService users;

  MockCLI cli;

  UserController user;

  @BeforeEach
  void setup() {
    user = new UserController(users);

    cli = new MockCLI();
  }

  @Test
  void byIdThrowsWithoutArguments() {
    var dummyUser = new User(33, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> user.byId(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(users);
  }

  @Test
  void byIdSuccess() {
    var dummyUser = new User(123, "username", "+7324123145", "test@example.com", "password", UserRole.CLIENT, 0);

    when(users.findById(123)).thenReturn(dummyUser);

    assertThat(user.byId(dummyUser, cli, "123")).isEqualTo("Found " + dummyUser.prettyFormat());

    cli.assertMatchesHistory();
    verify(users).findById(123);
  }

  @Test
  void createUserSuccess() {
    var dummyUser = new User(9, "username", "+7324123145", "test@example.com", "password", UserRole.ADMIN, 0);

    cli.out("Username > ").in("username")
       .out("Phone number > ").in("+12345678")
       .out("Email > ").in("test@x.com")
       .out("Password > ").in("password")
       .out("Role > ").in("");

    when(users.createUser(9, "username", "+12345678", "test@x.com", "password", UserRole.CLIENT)).thenReturn(dummyUser);

    user.create(dummyUser, cli);

    verify(users).createUser(9, "username", "+12345678", "test@x.com", "password", UserRole.CLIENT);
    cli.assertMatchesHistory();
  }

  @Test
  void userEditThrowsWithoutArguments() {
    var dummyUser = new User(123, "username", "+7324123145", "test@example.com", "password", UserRole.ADMIN, 0);

    assertThatThrownBy(() -> user.edit(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(users);
  }

  @Test
  void userEditSuccess() {
    var dummyAdmin = new User(54, "username", "+7324123145", "test@example.com", "password", UserRole.ADMIN, 0);
    var oldUser = new User(23, "username", "834242342", "test@ya.ru", "password", UserRole.CLIENT, 0);
    var newUser = new User(23, "newUsername", "834242342", "test@example.com", "newPassword", UserRole.ADMIN, 0);

    cli.out("Username (username) > ").in("newUsername")
       .out("Phone number (834242342) > ").in("")
       .out("Email (test@ya.ru) > ").in("test@example.com")
       .out("Password > ").in("newPassword")
       .out("Role (Client) > ").in("admin");

    when(users.findById(23)).thenReturn(oldUser);
    when(users.editUser(54, 23, "newUsername", null, "test@example.com", "newPassword", UserRole.ADMIN))
        .thenReturn(newUser);

    assertThat(user.edit(dummyAdmin, cli, "23")).isEqualTo("Saved changes to " + newUser.prettyFormat() + ".");

    cli.assertMatchesHistory();

    verify(users).editUser(54, 23, "newUsername", null, "test@example.com", "newPassword", UserRole.ADMIN);
  }
}
