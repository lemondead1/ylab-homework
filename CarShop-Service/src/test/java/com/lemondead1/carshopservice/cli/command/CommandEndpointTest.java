package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.controller.MockCLI;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandEndpointTest {
  @Mock
  Endpoint endpoint;

  MockCLI cli;
  CommandEndpoint command;

  @BeforeEach
  void setup() {
    cli = new MockCLI();
    command = new CommandEndpoint("testCommand", "testDescription", Set.of(UserRole.CLIENT), endpoint);
  }

  @Test
  void commandExecutesEndpointOnCorrectInputs() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    when(endpoint.execute(dummyUser, cli, "path")).thenReturn("printed");
    cli.out("printed\n");

    command.execute(dummyUser, cli, "path");

    verify(endpoint).execute(dummyUser, cli, "path");
    cli.assertMatchesHistory();
  }

  @Test
  void commandExecutesEndpointOnCorrectInputsAndPrintsNothingOnNull() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    when(endpoint.execute(dummyUser, cli, "path")).thenReturn(null);

    command.execute(dummyUser, cli, "path");

    verify(endpoint).execute(dummyUser, cli, "path");
    cli.assertMatchesHistory();
  }

  @Test
  void commandPrintsInsufficientPermissionsOnWrongUserRole() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);
    cli.out("Insufficient permissions.\n");

    command.execute(dummyUser, cli, "path");

    verifyNoInteractions(endpoint);
    cli.assertMatchesHistory();
  }

  @Test
  void commandPrintsHelpOnHelpSubcommand() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    cli.out("testDescription\n");

    command.execute(dummyUser, cli, "help");

    verifyNoInteractions(endpoint);
    cli.assertMatchesHistory();
  }

  @Test
  void commandPrintsHelpOnWrongUsageException() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    when(endpoint.execute(dummyUser, cli, "path")).thenThrow(new WrongUsageException());
    cli.out("testDescription\n");

    command.execute(dummyUser, cli, "path");

    cli.assertMatchesHistory();
  }

  @Test
  void commandPrintsMessageOnCommandException() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    when(endpoint.execute(dummyUser, cli, "path")).thenThrow(new CommandException("message"));
    cli.out("message\n");

    command.execute(dummyUser, cli, "path");

    cli.assertMatchesHistory();
  }
}
