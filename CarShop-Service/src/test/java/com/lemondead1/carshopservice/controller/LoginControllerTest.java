package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.service.*;
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

  MockConsoleIO cli;

  LoginController login;

  @BeforeEach
  void setup() {
    login = new LoginController(users);

    cli = new MockConsoleIO();
  }

  @Test
  void loginCallsUserServiceLogin() {
    cli.out("Username > ").in("username")
       .out("Password > ").in("password");

    assertThat(login.login(session, cli)).isEqualTo("Welcome, username!");

    cli.assertMatchesHistory();
  }

  @Test
  void logoutSetsCurrentUserTo0() {
    assertThat(login.logout(session, cli)).isEqualTo("Logout");

    cli.assertMatchesHistory();
    verify(session).setCurrentUserId(0);
  }

  @Test
  void signupCallsSignUserUp() {
    cli.out("Username > ").in("username")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(true);

    assertThat(login.signUp(session, cli)).isEqualTo("Signed up successfully!");

    cli.assertMatchesHistory();
    verify(users).signUserUp("username", "88005553535", "test@example.com", "password");
  }

  @Test
  void signupPrintsNameUsed() {
    cli.out("Username > ").in("username")
       .out("Username 'username' is already taken.\n")
       .out("Username > ").in("newusername")
       .out("Phone number > ").in("88005553535")
       .out("Email > ").in("test@example.com")
       .out("Password > ").in("password");

    when(users.checkUsernameFree("username")).thenReturn(false);
    when(users.checkUsernameFree("newusername")).thenReturn(true);

    assertThat(login.signUp(session, cli)).isEqualTo("Signed up successfully!");

    cli.assertMatchesHistory();
    verify(users).signUserUp("newusername", "88005553535", "test@example.com", "password");
  }
}
