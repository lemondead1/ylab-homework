package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.cli.parsing.EnumParser;
import com.lemondead1.carshopservice.cli.parsing.IntParser;
import com.lemondead1.carshopservice.cli.parsing.StringParser;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class UserController implements Controller {
  private final UserService users;

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.push("user").describe("Use 'user' to access user database.").allow(MANAGER, ADMIN)

           .accept("list", this::list)
           .describe("Use 'user list' to query users.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("create", this::create)
           .describe("Use 'user create' to create a new user.")
           .allow(ADMIN)
           .pop()

           .accept("edit", this::edit)
           .describe("Use 'user edit <id>' to change user fields.")
           .allow(ADMIN)
           .pop()

           .pop();
  }

  String list(SessionService session, ConsoleIO console, String... path) {
    var username = console.parseOptional("Username > ", StringParser.INSTANCE).orElse(null);
    var role = console.parseOptional("Role > ", EnumParser.of(UserRole.class)).orElse(null);
    var sort = console.parseOptional("Sorting > ", EnumParser.of(UserSorting.class)).orElse(UserSorting.USERNAME_ASC);
    var list = users.searchUsers(username, role, sort);
    var table = new TableFormatter("ID", "Username", "Role");
    for (var row : list) {
      table.addRow(row.id(), row.username(), row.role().getPrettyName());
    }
    return table.format(true);
  }

  String create(SessionService session, ConsoleIO console, String... path) {
    var username = console.parse("Username > ", StringParser.INSTANCE);
    var password = console.parse("Password > ", StringParser.INSTANCE);
    var role = console.parse("Role > ", EnumParser.of(UserRole.class, CLIENT, MANAGER, ADMIN));
    var newUser = users.createUser(session.getCurrentUserId(), username, password, role);
    return "Created " + newUser;
  }

  String edit(SessionService session, ConsoleIO console, String... path) {
    if (path.length == 0) {
      throw new CommandException("Usage: user edit <id>");
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var old = users.findById(id);
    var username = console.parseOptional("Username (" + old.username() + ") > ", StringParser.INSTANCE).orElse(null);
    var password = console.parseOptional("Password > ", StringParser.INSTANCE).orElse(null);
    var role = console.parseOptional("Role (" + old.role().getPrettyName() + ") > ",
                                     EnumParser.of(UserRole.class, CLIENT, MANAGER, ADMIN))
                      .orElse(null);
    var newUser = users.editUser(session.getCurrentUserId(), id, username, password, role);
    return "Modified " + newUser;
  }
}
