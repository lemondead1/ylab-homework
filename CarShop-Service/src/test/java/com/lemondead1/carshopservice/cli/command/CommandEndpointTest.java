package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.SessionService;
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

  @Mock
  ConsoleIO cli;

  @Mock
  SessionService session;

  CommandEndpoint command;

  @BeforeEach
  void setup() {
    command = new CommandEndpoint("testCommand", "testDescription", Set.of(UserRole.CLIENT), endpoint);
  }

  @Test
  void commandExecutesEndpointOnCorrectInputs() {
    String[] path = { "path" };

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(endpoint.execute(session, cli, path)).thenReturn("printed");
    command.execute(session, cli, path);

    verify(endpoint).execute(session, cli, path);
    verify(cli).println("printed");
  }

  @Test
  void commandExecutesEndpointOnCorrectInputsAndPrintsNothingOnNull() {
    String[] path = { "path" };

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(endpoint.execute(session, cli, path)).thenReturn(null);
    command.execute(session, cli, path);

    verify(endpoint).execute(session, cli, path);
    verifyNoInteractions(cli);
  }

  @Test
  void commandPrintsInsufficientPermissionsOnWrongUserRole() {
    String[] path = { "path" };

    when(session.getCurrentUserRole()).thenReturn(UserRole.ANONYMOUS);
    command.execute(session, cli, path);

    verifyNoInteractions(endpoint);
    verify(cli).println("Insufficient permissions.");
  }

  @Test
  void commandPrintsHelpOnHelpSubcommand() {
    String[] path = { "help" };

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    command.execute(session, cli, path);

    verifyNoInteractions(endpoint);
    verify(cli).println("testDescription");
  }

  @Test
  void commandPrintsHelpOnWrongUsageException() {
    String[] path = { "path" };
    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(endpoint.execute(session, cli, path)).thenThrow(new WrongUsageException());
    command.execute(session, cli, path);
    verify(cli).println("testDescription");
  }

  @Test
  void commandPrintsMessageOnCommandException() {
    String[] path = { "path" };

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(endpoint.execute(session, cli, path)).thenThrow(new CommandException("message"));

    command.execute(session, cli, path);

    verify(cli).println("message");
  }
}
