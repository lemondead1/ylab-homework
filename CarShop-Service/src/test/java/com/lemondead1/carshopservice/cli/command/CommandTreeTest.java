package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.controller.MockCLI;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

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

  MockCLI cli;

  CommandTree tree;

  @BeforeEach
  void beforeEach() {
    when(commandOne.getName()).thenReturn("one");
    when(commandTwo.getName()).thenReturn("two");
    when(commandThree.getName()).thenReturn("three");
    when(commandFour.getName()).thenReturn("four");

    cli = new MockCLI();
    tree = new CommandTree(List.of(commandOne, commandTwo, commandThree, commandFour),
                           "treeName",
                           "treeDescription",
                           Set.of(UserRole.CLIENT, UserRole.ADMIN));
  }

  @Test
  @DisplayName("execute 'two test' command passes 'test' to commandTwo.")
  void executeWithTwoTestExecutesCommandTwoWithTest() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    tree.execute(dummyUser, cli, "two", "test");
    verify(commandTwo).execute(dummyUser, cli, "test");
  }

  @Test
  @DisplayName("execute 'two test' by anonymous user prints 'Insufficient permissions.' and returns.")
  void executeTwoWithInsufficientPermissionsPrintsAndReturns() {
    var dummyUser = new User(1, "anonymous", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);
    cli.out("Insufficient permissions.\n");

    tree.execute(dummyUser, cli, "two", "test");

    verify(commandTwo, never()).execute(dummyUser, cli, "test");
    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("execute 'five' prints a command not found message.")
  void executeWithFivePrintsCommandNotFound() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    cli.out("Command 'five' not found. Use 'help' to list available commands.\n");

    tree.execute(dummyUser, cli, "five", "test");

    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("execute with no arguments prints help.")
  void executeWithNoArgsPrintsHelp() {
    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandThree.getDescription()).thenReturn("3");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    cli.out("treeDescription\n")
       .out("Subcommands:\n")
       .out("  one: 1\n")
       .out("  two: 2\n")
       .out("  three: 3\n")
       .out("  four: 4\n");

    tree.execute(dummyUser, cli);

    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("execute 'help' prints subcommand descriptions in order filtering by user role help.")
  void executeWithHelpPrintsDescriptionInOrderAndFiltersByUserRole() {
    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.ADMIN));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    cli.out("treeDescription\n")
       .out("Subcommands:\n")
       .out("  one: 1\n")
       .out("  two: 2\n")
       .out("  four: 4\n");

    tree.execute(dummyUser, cli, "help");

    cli.assertMatchesHistory();
  }
}
