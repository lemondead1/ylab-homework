package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.IntRangeConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.RoleSetConverter;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.IntRange;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class UserRepoTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
    dbManager.setupDatabase();
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @Test
  void createdUserMatchesSpec() {
    var created = users.create("duewngdaw", "88005553535", "test@x.com", "password", UserRole.ADMIN);
    assertThat(created)
        .isEqualTo(users.findById(created.id()))
        .isEqualTo(new User(created.id(), "duewngdaw", "88005553535", "test@x.com", "password", UserRole.ADMIN, 0));
  }

  @Test
  void creatingUsersWithTheSameUsernameThrows() {
    users.create("uewtwgfd", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThatThrownBy(() -> users.create("uewtwgfd", "88005553535", "test@example.com", "password", UserRole.CLIENT))
        .isInstanceOf(DBException.class);
  }

  @Test
  void editedUserMatchesSpec() {
    var created = users.create("qweq", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    var edited = users.edit(created.id(), null, "8912536173", null, "newPassword", UserRole.ADMIN);
    assertThat(edited)
        .isEqualTo(users.findById(created.id()))
        .isEqualTo(new User(created.id(), "qweq", "8912536173", "test@example.com", "newPassword", UserRole.ADMIN, 0));
  }

  @Test
  void editNotExistingUserThrows() {
    assertThatThrownBy(() -> users.edit(4636, "username", null, null, "password", UserRole.ADMIN))
        .isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void usernameConflictOnEditThrows() {
    users.create("connor", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    var second = users.create("steve", "88005553535", "test@example.com", "password", UserRole.ADMIN);
    assertThatThrownBy(() -> users.edit(second.id(), "connor", null, null, null, null)).isInstanceOf(DBException.class);
  }

  @Test
  void deleteReturnsOldUser() {
    var created = users.create("blaze", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.delete(created.id())).isEqualTo(created);
  }

  @Test
  void deleteThrowsOnAbsentId() {
    assertThatThrownBy(() -> users.delete(6534)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByIdThrowsAfterDelete() {
    var created = users.create("fermat", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    users.delete(created.id());
    assertThatThrownBy(() -> users.findById(created.id())).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameThrowsAfterDelete() {
    var created = users.create("michelangelo", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    users.delete(created.id());
    assertThatThrownBy(() -> users.findByUsername("user")).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameReturnsUser() {
    var created = users.create("donatello", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findByUsername("donatello")).isEqualTo(created);
  }

  @Test
  void findByIdReturnsUser() {
    var created = users.create("rafael", "88005553535", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(created.id())).isEqualTo(created);
  }

  @Test
  void deletingUserWithExistingOrdersThrows() {
    var car = cars.create("BMW", "X5", 2015, 3000000, "good");
    var user = users.create("leonardo", "88005553535", "test@example.com", "pwd", UserRole.CLIENT);
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user.id(), car.id(), "ASAP");
    assertThatThrownBy(() -> users.delete(user.id())).isInstanceOf(DBException.class);
  }

  @Nested
  class FilterTests {
    @BeforeEach
    void beforeEach() {
      dbManager.dropSchemas();
      dbManager.setupDatabase();
    }

    @ParameterizedTest
    @CsvSource({
        "'28, 119',                                              bard, ALL,   '', '', ALL",
        "'4, 32, 42, 44, 69, 78, 92, 97, 118, 125',              li,   ALL,   '', '', ALL",
        "'1, 4, 7, 22, 77, 81, 84, 96, 111, 112, 139',           '',   admin, '', '', ALL",
        "'13, 15, 37, 76, 82, 99, 102, 107, 108, 124, 141, 146', '',   ALL,   42, '', ALL",
        "'34, 66, 123',                                          '',   ALL,   '', ko, ALL",
        "'1, 2, 4, 8, 20, 36',                                   '',   ALL,   '', '', 2-3",
    })
    void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expected,
                    String username,
                    @ConvertWith(RoleSetConverter.class) Set<UserRole> roles,
                    String phoneNumber,
                    String email,
                    @ConvertWith(IntRangeConverter.class) IntRange purchases) {
      assertThat(users.lookup(username, roles, phoneNumber, email, purchases, UserSorting.USERNAME_ASC))
          .map(User::id).containsExactlyInAnyOrder(expected);
    }
  }

  @Test
  void sortingTestUsernameDesc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.USERNAME_DESC))
        .isSortedAccordingTo(Comparator.comparing(User::username, String::compareToIgnoreCase).reversed())
        .hasSize(users.totalCount());
  }

  @Test
  void sortingTestUsernameAsc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.USERNAME_ASC))
        .isSortedAccordingTo(Comparator.comparing(User::username, String::compareToIgnoreCase))
        .hasSize(users.totalCount());
  }

  @Test
  void sortingTestEmailDesc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.EMAIL_DESC))
        .isSortedAccordingTo(Comparator.comparing(User::email, String::compareToIgnoreCase).reversed())
        .hasSize(users.totalCount());
  }

  @Test
  void sortingTestEmailAsc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.EMAIL_ASC))
        .isSortedAccordingTo(Comparator.comparing(User::email, String::compareToIgnoreCase))
        .hasSize(users.totalCount());
  }

  @Test
  void sortingTestRoleDesc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.ROLE_DESC))
        .isSortedAccordingTo(Comparator.comparing(User::role).reversed()).hasSize(users.totalCount());
  }

  @Test
  void sortingTestRoleAsc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.ROLE_ASC))
        .isSortedAccordingTo(Comparator.comparing(User::role)).hasSize(users.totalCount());
  }

  @Test
  void sortingTestPurchasesDesc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.PURCHASES_DESC))
        .isSortedAccordingTo(Comparator.comparing(User::purchaseCount).reversed()).hasSize(users.totalCount());
  }

  @Test
  void sortingTestPurchasesAsc() {
    assertThat(users.lookup("", Set.copyOf(UserRole.AUTHORIZED), "", "", IntRange.ALL, UserSorting.PURCHASES_ASC))
        .isSortedAccordingTo(Comparator.comparing(User::purchaseCount)).hasSize(users.totalCount());
  }
}
