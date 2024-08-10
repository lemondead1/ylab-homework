package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.cli.validation.PatternValidator;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
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

           .accept("delete", this::delete)
           .describe("Use 'user delete <id>' to delete a user.")
           .allow(ADMIN)
           .pop()

           .pop();
  }

  String byId(User currentUser, CLI cli, String... args) {
    if (args.length == 0) {
      throw new WrongUsageException();
    }
    var user = IntParser.INSTANCE.map(users::findById).parse(args[0]);
    return "Found " + user.prettyFormat();
  }

  String search(User currentUser, CLI cli, String... path) {
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

  String create(User currentUser, CLI console, String... args) {
    var username = console.parse("Username > ", StringParser.INSTANCE, PatternValidator.USERNAME);
    var phoneNumber = console.parse("Phone number > ", StringParser.INSTANCE, PatternValidator.PHONE_NUMBER);
    var email = console.parse("Email > ", StringParser.INSTANCE, PatternValidator.EMAIL);
    var password = console.parse("Password > ", StringParser.INSTANCE, true, PatternValidator.PASSWORD);
    var role = console.parseOptional("Role > ", IdParser.of(CLIENT, MANAGER, ADMIN)).orElse(CLIENT);
    var newUser = users.createUser(currentUser.id(), username, phoneNumber, email, password, role);
    return "Created " + newUser.prettyFormat();
  }

  String edit(User currentUser, CLI cli, String... args) {
    if (args.length == 0) {
      throw new WrongUsageException();
    }
    var userToEdit = IntParser.INSTANCE.map(users::findById).parse(args[0]);

    var username = cli.parseOptional("Username (" + userToEdit.username() + ") > ", StringParser.INSTANCE).orElse(null);
    var phoneNumber = cli.parseOptional("Phone number (" + userToEdit.phoneNumber() + ") > ", StringParser.INSTANCE, PatternValidator.PHONE_NUMBER).orElse(null);
    var email = cli.parseOptional("Email (" + userToEdit.email() + ") > ", StringParser.INSTANCE, PatternValidator.EMAIL).orElse(null);
    var password = cli.parseOptional("Password > ", StringParser.INSTANCE, true).orElse(null);
    var role = cli.parseOptional("Role (" + userToEdit.role().getPrettyName() + ") > ", IdParser.of(CLIENT, MANAGER, ADMIN)).orElse(null);

    var newUser = users.editUser(currentUser.id(), userToEdit.id(), username, phoneNumber, email, password, role);
    return "Saved changes to " + newUser.prettyFormat() + ".";
  }

  String delete(User currentUser, CLI cli, String... args) {
    if (args.length == 0) {
      throw new WrongUsageException();
    }
    var userToDelete = IntParser.INSTANCE.map(users::findById).parse(args[0]);

    cli.printf("Deleting %s.", userToDelete.prettyFormat());

    if (!cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }

    try {
      users.deleteUser(currentUser.id(), userToDelete.id());
      return "Deleted";
    } catch (CascadingException e) {
      cli.println(e.getMessage());
    }

    if (!cli.parseOptional("Delete them [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }

    users.deleteUserCascading(currentUser.id(), userToDelete.id());
    return "Deleted";
  }
}
