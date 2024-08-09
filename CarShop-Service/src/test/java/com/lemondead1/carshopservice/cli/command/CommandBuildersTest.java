package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Mock
  ConsoleIO cli;

  CommandTreeRoot root;

  @BeforeEach
  void setup() {
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
  void rootExecutesEp1OnLogin() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);

    root.execute(dummyUser, cli, "login");

    verify(ep1).execute(dummyUser, cli);
    verifyNoInteractions(cli, ep2, ep3, ep4);
  }

  @Test
  void rootExecutesEp4OnUserDelete1() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    root.execute(dummyUser, cli, "user", "delete", "1");

    verify(ep4).execute(dummyUser, cli, "1");
    verifyNoInteractions(cli, ep1, ep2, ep3);
  }

  @Test
  void rootPrintsInsufficientPermissionsOnUserCreateWhenClient() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    root.execute(dummyUser, cli, "user", "create");

    verify(cli).println("Insufficient permissions.");
    verifyNoInteractions(ep1, ep2, ep3, ep4);
  }

  @Test
  void rootPrintsDescriptionInOrderFilteringByRole() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    root.execute(dummyUser, cli, "help");

    var capture = ArgumentCaptor.forClass(String.class);
    verify(cli, times(3)).println(capture.capture());

    assertThat(capture.getAllValues()).containsExactly("Subcommands:",
                                                       "  logout: Description 2",
                                                       "  user: Description 3");
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void treeBuildersThrowOnDuplicateSubcommand(boolean root) {
    var builder = root ? new CommandRootBuilder() : new TreeSubcommandBuilder<>(this.root, "name");
    assertThatThrownBy(() -> builder.accept("sub", ep1).pop().push("sub")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.push("sub").pop().accept("sub", ep2)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void treeBuildersThrowOnHelpSubcommand(boolean root) {
    var builder = root ? new CommandRootBuilder() : new TreeSubcommandBuilder<>(this.root, "name");
    assertThatThrownBy(() -> builder.push("help")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> builder.accept("help", ep1)).isInstanceOf(IllegalArgumentException.class);
  }
}
