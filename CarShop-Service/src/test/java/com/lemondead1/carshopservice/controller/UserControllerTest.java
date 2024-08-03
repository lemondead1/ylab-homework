package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
  @Mock
  UserService users;

  @Mock
  SessionService session;

  MockConsoleIO cli;

  UserController user;

  @BeforeEach
  void setup() {
    user = new UserController(users);

    cli = new MockConsoleIO();
  }

  @Test
  void createUserSuccess() {
    var dummyUser = new User(123, "username", "+7324123145", "test@example.com", "password", UserRole.CLIENT, 0);

    cli.out("Username > ").in("username")
       .out("Phone number > ").in("+7324123145")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password")
       .out("Role > ").in("");

    when(session.getCurrentUserId()).thenReturn(123);
    when(users.createUser(123, "username", "+7324123145", "test@example.com", "password", UserRole.CLIENT))
        .thenReturn(dummyUser);

    user.create(session, cli);

    cli.assertMatchesHistory();

    verify(users).createUser(123, "username", "+7324123145", "test@example.com", "password", UserRole.CLIENT);
  }

  @Test
  void userEditThrowsWithoutArguments() {
    assertThatThrownBy(() -> user.edit(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(users);
  }

  @Test
  void userEditSuccess() {
    var oldUser = new User(23, "username", "834242342", "test@ya.ru", "password", UserRole.CLIENT, 0);
    var newUser = new User(23, "newUsername", "834242342", "test@example.com", "newPassword", UserRole.ADMIN, 0);

    cli.out("Username (username) > ").in("newUsername")
       .out("Phone number (834242342) > ").in("")
       .out("Email (test@ya.ru) > ").in("test@example.com")
       .out("Password > ").in("newPassword")
       .out("Role (Client) > ").in("admin");

    when(session.getCurrentUserId()).thenReturn(54);
    when(users.findById(23)).thenReturn(oldUser);
    when(users.editUser(54, 23, "newUsername", null, "test@example.com", "newPassword", UserRole.ADMIN))
        .thenReturn(newUser);

    assertThat(user.edit(session, cli, "23")).isEqualTo("Saved " + newUser.prettyFormat());

    cli.assertMatchesHistory();

    verify(users).editUser(54, 23, "newUsername", null, "test@example.com", "newPassword", UserRole.ADMIN);
  }
}
