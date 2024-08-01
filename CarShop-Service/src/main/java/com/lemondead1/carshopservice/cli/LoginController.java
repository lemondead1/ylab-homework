package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.cli.parsing.StringParser;
import com.lemondead1.carshopservice.cli.validation.PatternValidator;
import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ValidationException;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;

public class LoginController implements Controller {
  private final UserService users;

  public LoginController(UserService users) {
    this.users = users;
  }

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.accept("signup", "Creates new user", this::signUp).allow(UserRole.ANONYMOUS).pop()
           .accept("login", "Logs you in", this::login).allow(UserRole.ANONYMOUS).pop();
  }

  private static final Validator<String> validUsername = new PatternValidator("[A-Za-z0-9_\\-.]{3,32}");
  private static final Validator<String> validPassword = new PatternValidator("[ -~]{8,}");

  String signUp(SessionService session, ConsoleIO cli, String... params) {
    String username = cli.parse("Username > ", StringParser.INSTANCE, validUsername, value -> {
      if (!users.checkUsernameFree(value)) {
        throw new ValidationException("Username '" + value + "' is already taken.");
      }
    });
    String password = cli.parse("Password > ", StringParser.INSTANCE, validPassword);
    users.signUserUp(username, password);
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
    return "Goodbye!";
  }
}
