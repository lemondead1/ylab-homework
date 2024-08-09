package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.entity.User;
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
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    root.execute(dummyUser, cli, "two", "test");
    verify(commandTwo).execute(dummyUser, cli, "test");
  }

  @Test
  void executeWithFivePrintsCommandNotFound() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    root.execute(dummyUser, cli, "five", "test");
    verify(cli).println("Command 'five' not found. Use 'help' to list available commands.");
  }

  @Test
  void executeWithNoArgsPrintsHelp() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandThree.getDescription()).thenReturn("3");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    root.execute(dummyUser, cli);

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
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    when(commandOne.getDescription()).thenReturn("1");
    when(commandTwo.getDescription()).thenReturn("2");
    when(commandFour.getDescription()).thenReturn("4");

    when(commandOne.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandTwo.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));
    when(commandThree.getAllowedRoles()).thenReturn(Set.of(UserRole.ADMIN));
    when(commandFour.getAllowedRoles()).thenReturn(Set.of(UserRole.CLIENT));

    root.execute(dummyUser, cli, "help");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(cli, times(4)).println(captor.capture());

    assertThat(captor.getAllValues()).containsExactly("Subcommands:",
                                                      "  one: 1",
                                                      "  two: 2",
                                                      "  four: 4");
  }
}
