package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePasswordException;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static UserRepo users;

  @Mock
  EventService eventService;

  SessionService session;

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

  @BeforeEach
  void beforeEach() {
    session = new SessionService(users, eventService);
  }

  @Test
  @DisplayName("login throws WrongUsernamePasswordException when called with wrong username.")
  void loginThrowsOnWrongUsername() {
    assertThatThrownBy(() -> session.login("wrongUsername", "pass")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  @DisplayName("login throws WrongUsernamePasswordException when called with wrong password.")
  void loginThrowsOnWrongPassword() {
    assertThatThrownBy(() -> session.login("admin", "wrongPass")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  @DisplayName("login updates the current user when called with correct credentials.")
  void successfulLoginTest() {
    session.login("admin", "password");

    assertThat(session.getCurrentUser()).isEqualTo(users.findById(1));
    verify(eventService).onUserLoggedIn(1);
  }

  @Test
  @DisplayName("logout sets the current user to an anonymous.")
  void logoutTest() {
    session.login("admin", "password");
    session.logout();

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }

  @Test
  @DisplayName("getCurrentUser returns an anonymous after deleting the current user.")
  void deletedUserTest() {
    session.login("spaylorcw", "hJ5=XuO!VO$|xP");

    users.delete(27);

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }

  @Test
  @DisplayName("signUserUp creates a user in the repo and calls EventService.onUserSignedUp.")
  void signUserUpCreatesUserAndPostsEvent() {
    var user = session.signUserUp("joebiden", "+73462684906", "test@example.com", "password");
    assertThat(users.findById(user.id())).isEqualTo(user);
    verify(eventService).onUserSignedUp(user);
  }

  @Test
  @DisplayName("signUserUp throws when called with a taken name.")
  void signUserUpThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> session.signUserUp("admin", "123456789", "test@x.com", "password"))
        .isInstanceOf(UserAlreadyExistsException.class);
  }
}
