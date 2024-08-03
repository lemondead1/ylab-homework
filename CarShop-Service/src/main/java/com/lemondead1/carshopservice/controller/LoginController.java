package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.StringParser;
import com.lemondead1.carshopservice.cli.validation.PatternValidator;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ValidationException;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginController implements Controller {
  private final UserService users;

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

  String signUp(SessionService session, ConsoleIO cli, String... params) {
    String username = cli.parse("Username > ", StringParser.INSTANCE, PatternValidator.USERNAME, value -> {
      if (!users.checkUsernameFree(value)) {
        throw new ValidationException("Username '" + value + "' is already taken.");
      }
    });
    String phoneNumber = cli.parse("Phone number > ", StringParser.INSTANCE, PatternValidator.PHONE_NUMBER);
    String email = cli.parse("Phone number > ", StringParser.INSTANCE, PatternValidator.EMAIL);
    String password = cli.parse("Password > ", StringParser.INSTANCE, PatternValidator.PASSWORD);
    users.signUserUp(username, phoneNumber, email, password);
    return "Signed up successfully!";
  }

  String login(SessionService session, ConsoleIO cli, String... params) {
    String username = cli.parse("Username > ", StringParser.INSTANCE);
    String password = cli.parse("Password > ", StringParser.INSTANCE);
    users.login(username, password, session);
    return "Welcome, " + username + "!";
  }

  String logout(SessionService session, ConsoleIO cli, String... params) {
    session.setCurrentUserId(0);
    return "Logout";
  }
}
