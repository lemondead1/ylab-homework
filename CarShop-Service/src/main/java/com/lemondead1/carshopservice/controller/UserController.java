package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.cli.validation.PatternValidator;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class UserController implements Controller {
  private final UserService users;

  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.push("user").describe("Use 'user' to access user database.").allow(MANAGER, ADMIN)

           .accept("search", this::search)
           .describe("Use 'user search' to query users.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("create", this::create)
           .describe("Use 'user create' to create a new user.")
           .allow(ADMIN)
           .pop()

           .accept("edit", this::edit)
           .describe("Use 'user edit <id>' to edit user fields.")
           .allow(ADMIN)
           .pop()

           .accept("by-id", this::byId)
           .describe("Use 'user by-id <id>' to lookup users by id.")
           .allow(MANAGER, ADMIN)
           .pop()

           .pop();
  }

  String byId(User currentUser, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    var user = IntParser.INSTANCE.map(users::findById).parse(path[0]);
    return "Found " + user.prettyFormat();
  }

  String search(User currentUser, ConsoleIO cli, String... path) {
    var username = cli.parseOptional("Username > ", StringParser.INSTANCE).orElse("");
    var role = cli.parseOptional("Role > ", IdListParser.of(UserRole.class)).orElse(UserRole.AUTHORIZED);
    var phoneNumber = cli.parseOptional("Phone number > ", StringParser.INSTANCE).orElse("");
    var email = cli.parseOptional("Email > ", StringParser.INSTANCE).orElse("");
    var purchases = cli.parseOptional("Purchases > ", IntRangeParser.INSTANCE).orElse(IntRange.ALL);
    var sort = cli.parseOptional("Sorting > ", IdParser.of(UserSorting.class)).orElse(UserSorting.USERNAME_ASC);
    var list = users.lookupUsers(username, role, phoneNumber, email, purchases, sort);
    var table = new TableFormatter("ID", "Username", "Phone number", "Email", "Purchase count", "Role");
    for (var row : list) {
      table.addRow(row.id(), row.username(), row.phoneNumber(), row.email(), row.purchaseCount(),
                   row.role().getPrettyName());
    }
    return table.format(true);
  }

  String create(User currentUser, ConsoleIO console, String... path) {
    var username = console.parse("Username > ", StringParser.INSTANCE, PatternValidator.USERNAME);
    var phoneNumber = console.parse("Phone number > ", StringParser.INSTANCE, PatternValidator.PHONE_NUMBER);
    var email = console.parse("Email > ", StringParser.INSTANCE, PatternValidator.EMAIL);
    var password = console.parse("Password > ", StringParser.INSTANCE, true, PatternValidator.PASSWORD);
    var role = console.parseOptional("Role > ", IdParser.of(CLIENT, MANAGER, ADMIN)).orElse(CLIENT);
    var newUser = users.createUser(currentUser.id(), username, phoneNumber, email, password, role);
    return "Created " + newUser.prettyFormat();
  }

  String edit(User currentUser, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    var old = IntParser.INSTANCE.map(users::findById).parse(path[0]);
    var username = cli.parseOptional("Username (" + old.username() + ") > ", StringParser.INSTANCE).orElse(null);
    var phoneNumber = cli.parseOptional("Phone number (" + old.phoneNumber() + ") > ",
                                        StringParser.INSTANCE, PatternValidator.PHONE_NUMBER)
                         .orElse(null);
    var email = cli.parseOptional("Email (" + old.email() + ") > ", StringParser.INSTANCE, PatternValidator.EMAIL)
                   .orElse(null);
    var password = cli.parseOptional("Password > ", StringParser.INSTANCE, true).orElse(null);
    var role = cli.parseOptional("Role (" + old.role().getPrettyName() + ") > ", IdParser.of(CLIENT, MANAGER, ADMIN))
                  .orElse(null);
    var newUser = users.editUser(currentUser.id(), old.id(), username, phoneNumber, email, password, role);
    return "Saved " + newUser.prettyFormat();
  }
}
