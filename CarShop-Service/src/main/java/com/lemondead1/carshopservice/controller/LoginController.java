package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.StringParser;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ValidationException;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginController implements Controller {
  private final SessionService session;

  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.accept("signup", this::signUp)
           .describe("Use 'signup' to sign up.")
           .allow(UserRole.ANONYMOUS)
           .pop()

           .accept("logout", this::logout)
           .describe("Use 'logout' to log out.")
           .allow(UserRole.CLIENT, UserRole.MANAGER, UserRole.ADMIN)
           .pop()

           .accept("login", this::login)
           .describe("Use 'login' to log in.")
           .allow(UserRole.ANONYMOUS)
           .pop();
  }

  String signUp(User currentUser, CLI cli, String... args) {
    String username = cli.parse("Username > ", StringParser.INSTANCE, Util.USERNAME, value -> {
      if (!session.checkUsernameFree(value)) {
        throw new ValidationException("Username '" + value + "' is already taken.");
      }
    });
    String phoneNumber = cli.parse("Phone number > ", StringParser.INSTANCE, Util.PHONE_NUMBER);
    String email = cli.parse("Email > ", StringParser.INSTANCE, Util.EMAIL);
    String password = cli.parse("Password > ", StringParser.INSTANCE, true, Util.PASSWORD);
    session.signUserUp(username, phoneNumber, email, password);
    return "Signed up successfully!";
  }

  String login(User currentUser, CLI cli, String... args) {
    String username = cli.parse("Username > ", StringParser.INSTANCE);
    String password = cli.parse("Password > ", StringParser.INSTANCE, true);
    session.login(username, password);
    return "Welcome, " + username + "!";
  }

  String logout(User currentUser, CLI cli, String... args) {
    session.logout();
    return "Logout";
  }
}
