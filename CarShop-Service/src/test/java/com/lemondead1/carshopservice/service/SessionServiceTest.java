package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePasswordException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static UserRepo users;
  static OrderRepo orders;

  @Mock
  EventService eventService;

  SessionService session;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
    dbManager.setupDatabase();
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @BeforeEach
  void setup() {
    session = new SessionService(users, eventService);
  }

  @Test
  void loginThrowsOnWrongUsername() {
    assertThatThrownBy(() -> session.login("wrongUsername", "pass")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void loginThrowsOnWrongPassword() {
    assertThatThrownBy(() -> session.login("admin", "wrongPass")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void successfulLoginTest() {
    session.login("admin", "password");

    assertThat(session.getCurrentUser()).isEqualTo(users.findById(1));
    verify(eventService).onUserLoggedIn(1);
  }

  @Test
  void logoutTest() {
    session.login("admin", "password");
    session.logout();

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }

  @Test
  void deletedUserTest() {
    session.login("spaylorcw", "hJ5=XuO!VO$|xP");

    users.delete(27);

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }

  @Test
  void signUserUpCreatesUserAndPostsEvent() {
    var user = session.signUserUp("joebiden", "+73462684906", "test@example.com", "password");
    assertThat(users.findById(user.id())).isEqualTo(user);
    verify(eventService).onUserSignedUp(user);
  }

  @Test
  void signUserUpThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> session.signUserUp("admin", "123456789", "test@x.com", "password"))
        .isInstanceOf(UserAlreadyExistsException.class);
  }
}
