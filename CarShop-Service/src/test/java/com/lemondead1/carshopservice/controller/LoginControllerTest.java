package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {
  @Mock
  SessionService session;

  @Mock
  UserService users;

  MockCLI cli;

  LoginController login;

  @BeforeEach
  void setup() {
    login = new LoginController(users, session);

    cli = new MockCLI();
  }

  @Test
  void loginCallsUserServiceLogin() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    cli.out("Username > ").in("username")
       .out("Password > ").in("password");

    assertThat(login.login(dummyUser, cli)).isEqualTo("Welcome, username!");

    cli.assertMatchesHistory();
  }

  @Test
  void logoutSetsCurrentUserTo0() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThat(login.logout(dummyUser, cli)).isEqualTo("Logout");

    verify(session).logout();
    cli.assertMatchesHistory();
  }

  @Test
  void signupCallsSignUserUp() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);
    cli.out("Username > ").in("username")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(true);

    assertThat(login.signUp(dummyUser, cli)).isEqualTo("Signed up successfully!");

    verify(users).signUserUp("username", "88005553535", "test@example.com", "password");
    cli.assertMatchesHistory();
  }

  @Test
  void signupPrintsNameUsed() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);
    cli.out("Username > ").in("username")
       .out("Username 'username' is already taken.\n")
       .out("Username > ").in("newusername")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(false);
    when(users.checkUsernameFree("newusername")).thenReturn(true);

    assertThat(login.signUp(dummyUser, cli)).isEqualTo("Signed up successfully!");

    cli.assertMatchesHistory();
    verify(users).signUserUp("newusername", "88005553535", "test@example.com", "password");
  }
}
