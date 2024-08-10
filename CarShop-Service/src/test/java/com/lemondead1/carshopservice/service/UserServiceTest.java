package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
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
public class UserServiceTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;
  static EventRepo events;

  @Mock
  EventService eventService;
  UserService userService;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
    dbManager.setupDatabase();
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
    events = new EventRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @BeforeEach
  void beforeEach() {
    userService = new UserService(users, orders, eventService);
  }

  @Test
  void createUserSavesUserAndPostsEvent() {
    var user = userService.createUser(3, "obemna", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(user.id())).isEqualTo(user);
    verify(eventService).onUserCreated(3, user);
  }

  @Test
  void createUserThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> userService.createUser(1, "admin", "123456789", "test@x.com", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void createUserThrowsOnAnonymousRole() {
    assertThatThrownBy(() -> userService.createUser(1, "newname", "123456789", "test@x.com",
                                                    "password", UserRole.ANONYMOUS))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void signUserUpCreatesUserAndPostsEvent() {
    var user = userService.signUserUp("joebiden", "+73462684906", "test@example.com", "password");
    assertThat(users.findById(user.id())).isEqualTo(user);
    verify(eventService).onUserSignedUp(user);
  }

  @Test
  void signUserUpThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> userService.signUserUp("admin", "123456789", "test@x.com", "password"))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void editSavesNewUserAndPostsAnEvent() {
    var oldUser = userService.createUser(3, "steve", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    var newUser = userService.editUser(5, oldUser.id(), "bob", "+5334342", "test@ya.com", "password", UserRole.CLIENT);
    assertThat(users.findById(oldUser.id())).isEqualTo(newUser);
    verify(eventService).onUserEdited(5, oldUser, newUser);
  }

  @Test
  void editThrowsOnNonExistentUser() {
    assertThatThrownBy(() -> userService.editUser(5, 500, "newUsername", "+5334342",
                                                  "test1@example.com", "password", UserRole.CLIENT))
        .isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void editThrowsOnAnonymousRole() {
    assertThatThrownBy(() -> userService.editUser(1, 1, null, null, null, null, UserRole.ANONYMOUS))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void deleteUserDeletesUserAndPostsAnEvent() {
    userService.deleteUser(1, 78);
    assertThatThrownBy(() -> users.findById(78));
    verify(eventService).onUserDeleted(1, 78);
  }
}
