package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.controller.MockCLI;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandBuildersTest {
  @Mock
  Endpoint ep1; // login

  @Mock
  Endpoint ep2; // logout

  @Mock
  Endpoint ep3; // user create

  @Mock
  Endpoint ep4; // user delete <id>

  MockCLI cli;

  CommandTreeRoot root;

  @BeforeEach
  void beforeEach() {
    cli = new MockCLI();
    root = new CommandRootBuilder().accept("login", ep1)
                                   .allow(UserRole.ANONYMOUS)
                                   .describe("Description 1")
                                   .pop()

                                   .accept("logout", ep2)
                                   .allow(UserRole.CLIENT, UserRole.ADMIN, UserRole.MANAGER)
                                   .describe("Description 2")
                                   .pop()

                                   .push("user").allow(UserRole.ADMIN, UserRole.MANAGER).describe("Description 3")

                                   .accept("create", ep3).allow(UserRole.MANAGER, UserRole.ADMIN).pop()
                                   .accept("delete", ep4).allow(UserRole.ADMIN).pop()

                                   .pop()
                                   .build();
  }

  @Test
  @DisplayName("CommandTreeRoot dispatches 'login' command to ep1.")
  void rootExecutesEp1OnLogin() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);

    root.execute(dummyUser, cli, "login");

    verify(ep1).execute(dummyUser, cli);
    verifyNoMoreInteractions(ep1, ep2, ep3, ep4);
    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("CommandTreeRoot dispatches 'user delete 1' command to ep4.")
  void rootExecutesEp4OnUserDelete1() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    root.execute(dummyUser, cli, "user", "delete", "1");

    verify(ep4).execute(dummyUser, cli, "1");
    verifyNoMoreInteractions(ep1, ep2, ep3, ep4);
    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("CommandTreeRoot prints 'Insufficient permissions.\\n' when client executes command 'user create'.")
  void rootPrintsInsufficientPermissionsOnUserCreateWhenClient() {
    cli.out("Insufficient permissions.\n");
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    root.execute(dummyUser, cli, "user", "create");

    verifyNoInteractions(ep1, ep2, ep3, ep4);
    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("CommandTreeRoot prints descriptions of subcommands in order filtering by their allowed roles.")
  void rootPrintsDescriptionInOrderFilteringByRole() {
    cli.out("Subcommands:\n")
       .out("  logout: Description 2\n")
       .out("  user: Description 3\n");
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    root.execute(dummyUser, cli, "help");

    cli.assertMatchesHistory();
  }

  @Test
  @DisplayName("CommandRootBuilder throws on attempt to add a conflicting subcommand.")
  void treeRootBuilderThrowOnDuplicateSubcommand() {
    var builder = new CommandRootBuilder();
    assertThatThrownBy(() -> builder.accept("sub", ep1).pop().push("sub")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.push("sub").pop().accept("sub", ep2)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("TreeSubcommandBuilder throws on attempt to add a conflicting subcommand.")
  void subcommandTreeBuilderThrowOnDuplicateSubcommand() {
    var builder = new TreeSubcommandBuilder<>(this.root, "name");
    assertThatThrownBy(() -> builder.accept("sub", ep1).pop().push("sub")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.push("sub").pop().accept("sub", ep2)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("CommandRootBuilder throws on attempt to add a 'help' subcommand.")
  void treeBuildersThrowOnHelpSubcommand() {
    var builder = new CommandRootBuilder();
    assertThatThrownBy(() -> builder.push("help")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.accept("help", ep1)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("TreeSubcommandBuilder throws on attempt to add a 'help' subcommand.")
  void treeSubcommandBuilderThrowOnHelpSubcommand() {
    var builder = new TreeSubcommandBuilder<>(this.root, "name");
    assertThatThrownBy(() -> builder.push("help")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.accept("help", ep1)).isInstanceOf(IllegalArgumentException.class);
  }
}
