package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Set;

/**
 * Checks user permissions and performs command specified by endpoint.
 */
@RequiredArgsConstructor
public class CommandEndpoint implements Command {
  private final String name;
  private final String description;
  private final Set<UserRole> allowedRoles;
  private final Endpoint endpoint;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Collection<UserRole> getAllowedRoles() {
    return allowedRoles;
  }

  @Override
  public void execute(User currentUser, CLI cli, String... path) {
    if (!getAllowedRoles().contains(currentUser.role())) {
      cli.println("Insufficient permissions.");
      return;
    }

    try {
      if (path.length >= 1 && "help".equals(path[0])) {
        cli.println(description);
      } else {
        var result = endpoint.execute(currentUser, cli, path);
        if (result != null) {
          cli.println(result);
        }
      }
    } catch (WrongUsageException e) {
      cli.println(description);
    } catch (CommandException e) {
      cli.println(e.getMessage());
    }
  }
}
