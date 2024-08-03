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

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CommandTreeTest {
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

  CommandTree tree;

  @BeforeEach
  void setup() {
    when(commandOne.getName()).thenReturn("one");
    when(commandTwo.getName()).thenReturn("two");
    when(commandThree.getName()).thenReturn("three");
    when(commandFour.getName()).thenReturn("four");

    tree = new CommandTree(List.of(commandOne, commandTwo, commandThree, commandFour),
                           "treeName",
                           "treeDescription",
                           Set.of(UserRole.CLIENT));
  }

  @Test
  void executeWithTwoTestExecutesCommandTwoWithTest() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    tree.execute(session, cli, "two", "test");
    verify(commandTwo).execute(session, cli, "test");
  }

  @Test
  void executeTwoWithInsufficientPermissionsPrintsAndReturns() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.ANONYMOUS);
    tree.execute(session, cli, "two", "test");
    verify(cli).println("Insufficient permissions.");
    verify(commandTwo, never()).execute(session, cli, "test");
  }

  @Test
  void executeWithFivePrintsCommandNotFound() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    tree.execute(session, cli, "five", "test");
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

    tree.execute(session, cli);

    var captor = ArgumentCaptor.forClass(String.class);
    verify(cli, times(6)).println(captor.capture());

    assertThat(captor.getAllValues()).containsExactly("treeDescription",
                                                      "Subcommands:",
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

    tree.execute(session, cli, "help");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(cli, times(5)).println(captor.capture());

    assertThat(captor.getAllValues()).containsExactly("treeDescription",
                                                      "Subcommands:",
                                                      "  one: 1",
                                                      "  two: 2",
                                                      "  four: 4");
  }
}
