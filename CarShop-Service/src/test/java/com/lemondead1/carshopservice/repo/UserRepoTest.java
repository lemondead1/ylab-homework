package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.HasIdEnumConverter;
import com.lemondead1.carshopservice.HasIdEnumSetConverter;
import com.lemondead1.carshopservice.IntRangeConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserRepoTest {
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static UserRepo users;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(),
                              postgres.getPassword(), "data", "infra", "db/changelog/test-changelog.yaml", true);
    dbManager.setupDatabase();
    users = new UserRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @ParameterizedTest
  @CsvSource({
      "25, bclague6o,   92093212299, bcudERioam@example.com, 'wR0.,@m>2U',    manager, 1",
      "12, cfunnellmd,  27067260151, aDy7IStAv8@example.com, zC8/!I83%AKz),   admin,   1",
      "3,  eledstonefz, 67510438945, uFnB7iSehx@example.com, uM2+FrMY=9h@=o6, client,  3",
      "16, sbaythorpg7, 26692163281, jkwziZqaRD@example.com, xT6|D1(3+~IrC1r, client,  0"
  })
  @DisplayName("findById returns the correct user.")
  void findByIdReturnsCorrectUser(int id,
                                  String username,
                                  String phoneNumber,
                                  String email,
                                  String password,
                                  @ConvertWith(HasIdEnumConverter.class) UserRole role,
                                  int purchaseCount) {
    assertThat(users.findById(id)).isEqualTo(new User(id, username, phoneNumber, email, password, role, purchaseCount));
  }

  @Test
  @DisplayName("create creates a user matching arguments.")
  void createdUserMatchesSpec() {
    var created = users.create("duewngdaw", "88005553535", "test@x.com", "password", UserRole.ADMIN);
    assertThat(created)
        .isEqualTo(users.findById(created.id()))
        .isEqualTo(new User(created.id(), "duewngdaw", "88005553535", "test@x.com", "password", UserRole.ADMIN, 0));
  }

  @Test
  @DisplayName("create throws when there already exists a user with the given username.")
  void creatingUsersWithTheSameUsernameThrows() {
    users.create("uewtwgfd", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThatThrownBy(() -> users.create("uewtwgfd", "88005553535", "test@example.com", "password", UserRole.CLIENT))
        .isInstanceOf(DBException.class);
  }

  @Test
  @DisplayName("edit changes user according to the non-null arguments.")
  void editedUserMatchesSpec() {
    var created = users.create("qweq", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    var edited = users.edit(created.id(), null, "8912536173", null, "newPassword", UserRole.ADMIN);
    assertThat(edited)
        .isEqualTo(users.findById(created.id()))
        .isEqualTo(new User(created.id(), "qweq", "8912536173", "test@example.com", "newPassword", UserRole.ADMIN, 0));
  }

  @Test
  @DisplayName("edit throws when a user with the requested id does not exist.")
  void editNotExistingUserThrows() {
    assertThatThrownBy(() -> users.edit(4636, "username", null, null, "password", UserRole.ADMIN))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("edit throws when there already exists a user with the requested username.")
  void usernameConflictOnEditThrows() {
    users.create("connor", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    var second = users.create("steve", "88005553535", "test@example.com", "password", UserRole.ADMIN);
    assertThatThrownBy(() -> users.edit(second.id(), "connor", null, null, null, null)).isInstanceOf(DBException.class);
  }

  @Test
  @DisplayName("delete deletes the user.")
  void deleteReturnsOldUser() {
    var created = users.create("blaze", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.delete(created.id())).isEqualTo(created);
    assertThatThrownBy(() -> users.findById(created.id())).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> users.findByUsername("user")).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("delete throws RowNotFoundException when a user with the given id does not exist.")
  void deleteThrowsOnAbsentId() {
    assertThatThrownBy(() -> users.delete(6534)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("findByUsername returns the correct user.")
  void findByUsernameReturnsUser() {
    var created = users.create("donatello", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findByUsername("donatello")).isEqualTo(created);
  }

  @Test
  @DisplayName("findById returns the correct user.")
  void findByIdReturnsUser() {
    var created = users.create("rafael", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(created.id())).isEqualTo(created);
  }

  @Test
  @DisplayName("delete throws when there exist orders referencing the given user.")
  void deletingUserWithExistingOrdersThrows() {
    assertThatThrownBy(() -> users.delete(1)).isInstanceOf(DBException.class);
  }

  @Nested
  class LookupTests {
    @BeforeAll
    static void beforeAll() {
      dbManager.dropSchemas();
      dbManager.setupDatabase();
    }

    @ParameterizedTest
    @CsvSource({
        "'10, 50',                                        ter, 'client, manager, admin', '', '', ALL",
        "'17, 36, 39, 54, 65, 66, 100, 133, 145',         li,  'client, manager, admin', '', '', ALL",
        "'1, 12, 24',                                     '',  admin,                    '', '', 1-3",
        "'17, 21, 30, 52, 55, 58, 75, 84, 103, 113, 147', '',  'client, manager, admin', 42, '', ALL",
        "'20, 129',                                       '',  'client, manager, admin', '', ab, ALL",
        "'2, 3, 10, 11, 15',                              '',  'client, manager, admin', '', '', 2-3",
    })
    @DisplayName("lookup filters rows according to arguments.")
    void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expected,
                    String username,
                    @ConvertWith(HasIdEnumSetConverter.class) Set<UserRole> roles,
                    String phoneNumber,
                    String email,
                    @ConvertWith(IntRangeConverter.class) IntRange purchases) {
      assertThat(users.lookup(username, roles, phoneNumber, email, purchases, UserSorting.USERNAME_ASC))
          .map(User::id).containsExactlyInAnyOrder(expected);
    }

    @Test
    void sortingTestUsernameDesc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.USERNAME_DESC))
          .isSortedAccordingTo(Comparator.comparing(User::username, String::compareToIgnoreCase).reversed())
          .hasSize(151);
    }

    @Test
    void sortingTestUsernameAsc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.USERNAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(User::username, String::compareToIgnoreCase))
          .hasSize(151);
    }

    @Test
    void sortingTestEmailDesc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.EMAIL_DESC))
          .isSortedAccordingTo(Comparator.comparing(User::email, String::compareToIgnoreCase).reversed())
          .hasSize(151);
    }

    @Test
    void sortingTestEmailAsc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.EMAIL_ASC))
          .isSortedAccordingTo(Comparator.comparing(User::email, String::compareToIgnoreCase))
          .hasSize(151);
    }

    @Test
    void sortingTestRoleDesc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.ROLE_DESC))
          .isSortedAccordingTo(Comparator.comparing(User::role).reversed()).hasSize(151);
    }

    @Test
    void sortingTestRoleAsc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.ROLE_ASC))
          .isSortedAccordingTo(Comparator.comparing(User::role)).hasSize(151);
    }

    @Test
    void sortingTestPurchasesDesc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.PURCHASES_DESC))
          .isSortedAccordingTo(Comparator.comparing(User::purchaseCount).reversed()).hasSize(151);
    }

    @Test
    void sortingTestPurchasesAsc() {
      assertThat(users.lookup("", Set.copyOf(UserRole.ALL), "", "", IntRange.ALL, UserSorting.PURCHASES_ASC))
          .isSortedAccordingTo(Comparator.comparing(User::purchaseCount)).hasSize(151);
    }
  }
}
