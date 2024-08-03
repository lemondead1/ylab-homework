package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CommandTreeRootTest {
  @Mock
  Command commandOne;

  @Mock
  Command commandTwo;

  @Mock
  Command commandThree;

  @Mock
  Command commandFour;

  @Mock
  SessionService session;

  @Mock
  ConsoleIO cli;

  CommandTreeRoot root;

  @BeforeEach
  void setup() {
    when(commandOne.getName()).thenReturn("one");
    when(commandTwo.getName()).thenReturn("two");
    when(commandThree.getName()).thenReturn("three");
    when(commandFour.getName()).thenReturn("four");

    root = new CommandTreeRoot(List.of(commandOne, commandTwo, commandThree, commandFour));
  }

  @Test
  void executeWithOneTestExecutesCommandTwoWithTest() {
    root.execute(session, cli, "two", "test");
    verify(commandTwo).execute(session, cli, "test");
  }

  @Test
  void executeWithFivePrintsCommandNotFound() {
    root.execute(session, cli, "five", "test");
    verify(cli).println("Command 'five' not found. Use 'help' to list available commands.");
  }

  @Test
  void executeWithNoArgsPrintsHelp() {
    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandThree.getDescription()).thenReturn("3");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);

    root.execute(session, cli);

    var captor = ArgumentCaptor.forClass(String.class);
    verify(cli, times(5)).println(captor.capture());

    assertThat(captor.getAllValues()).containsExactly("Subcommands:",
                                                      "  one: 1",
                                                      "  two: 2",
                                                      "  three: 3",
                                                      "  four: 4");
  }

  @Test
  void executeWithHelpPrintsDescriptionInOrderAndFiltersByUserRole() {
    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.ADMIN));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);

    root.execute(session, cli, "help");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(cli, times(4)).println(captor.capture());

    assertThat(captor.getAllValues()).containsExactly("Subcommands:",
                                                      "  one: 1",
                                                      "  two: 2",
                                                      "  four: 4");
  }
}
