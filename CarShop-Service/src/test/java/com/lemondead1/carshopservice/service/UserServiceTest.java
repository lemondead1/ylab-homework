package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.DBConnector;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.aspectj.lang.Aspects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  private static final UserRepo users = DBConnector.USER_REPO;
  private static final OrderRepo orders = DBConnector.ORDER_REPO;

  @Mock
  EventService eventService;

  UserService userService;

  private final User dummyUser = new User(5, "dummy", "123456789", "dummy@example.com", "password", UserRole.ADMIN, 0);

  @BeforeEach
  void beforeEach() {
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(() -> dummyUser);
    Aspects.aspectOf(AuditedAspect.class).setEventService(eventService);
    userService = new UserService(users, orders);
  }

  @AfterEach
  void afterEach() {
    DBConnector.DB_MANAGER.rollback();
  }

  @Test
  @DisplayName("createUser saves user into the repo and calls EventService.onUserCreated.")
  void createUserSavesUserAndPostsEvent() {
    var user = userService.createUser("obemna", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(user.id())).isEqualTo(user);
    verify(eventService).postEvent(eq(5), eq(EventType.USER_CREATED), any());
  }

  @Test
  @DisplayName("createUser throws UserAlreadyExistsException when attempting to add a user with a taken username.")
  void createUserThrowsOnDuplicateUsername() {
    assertThatThrownBy(() -> userService.createUser("admin", "123456789", "test@x.com", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("editUser edits the user in the repo and calls EventService.onUserEdited.")
  void editSavesNewUserAndPostsAnEvent() {
    var oldUser = userService.createUser("steve", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    var newUser = userService.editUser(oldUser.id(), "bob", "+5334342", "test@ya.com", "password", UserRole.CLIENT);
    assertThat(users.findById(oldUser.id()))
        .isEqualTo(newUser)
        .isEqualTo(new User(oldUser.id(), "bob", "+5334342", "test@ya.com", "password", UserRole.CLIENT, 0));
    verify(eventService).postEvent(eq(5), eq(EventType.USER_MODIFIED), any());
  }

  @Test
  @DisplayName("editUser throws a RowNotFoundException when userId is not found.")
  void editThrowsOnNonExistentUser() {
    assertThatThrownBy(() -> userService.editUser(500, "newUsername", "+5334342", null, null, UserRole.CLIENT))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("deleteUser deletes user from the repo and calls EventService.onUserDeleted.")
  void deleteUserDeletesUserAndPostsAnEvent() {
    userService.deleteUser(78);
    assertThatThrownBy(() -> users.findById(78)).isInstanceOf(NotFoundException.class);
    verify(eventService).postEvent(eq(5), eq(EventType.USER_DELETED), any());
  }

  @Test
  @DisplayName("deleteUserCascade deletes the user and associated orders and submits events.")
  void deleteUserCascadeTest() {
    userService.deleteUserCascading(18);

    assertThatThrownBy(() -> users.findById(18)).isInstanceOf(NotFoundException.class);
    assertThatThrownBy(() -> orders.findById(253)).isInstanceOf(NotFoundException.class);

    verify(eventService).postEvent(eq(5), eq(EventType.USER_DELETED), any());
  }
}
