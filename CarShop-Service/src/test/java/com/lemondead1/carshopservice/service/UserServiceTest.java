package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePasswordException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest {
  CarRepo cars;
  UserRepo users;
  OrderRepo orders;
  EventRepo events;
  EventService eventService;
  UserService userService;
  SessionService session;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo();
    events = new EventRepo();
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
    events.setUsers(users);
    eventService = new EventService(events, new TimeService());
    userService = new UserService(users, eventService);
    session = new SessionService(userService);
  }

  @Test
  void createUserSavesUserAndPostsEvent() {
    var user = userService.createUser(3, "username", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(1)).isEqualTo(user);
    assertThat(events.lookup(Set.of(EventType.USER_CREATED), DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .hasSize(1).map(e -> (UserEvent.Created) e).allMatch(e -> e.getUserId() == 3 && e.getCreatedUserId() == 1);
  }

  @Test
  void signUserUpCreatesUserAndPostsEvent() {
    var user = userService.signUserUp("username", "+73462684906", "test@example.com", "password");
    assertThat(users.findById(1)).isEqualTo(user);
    assertThat(events.lookup(Set.of(EventType.USER_SIGNED_UP), DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .hasSize(1).map(e -> (UserEvent.SignUp) e).allMatch(e -> e.getUserId() == 1);
  }

  @Test
  void loginThrowsWrongUsernamePasswordOnWrongUsername() {
    userService.signUserUp("username", "+73462684906", "test@example.com", "password");
    assertThatThrownBy(() -> userService.login("doesnotexist", "password", session))
        .isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void loginThrowsWrongUsernamePasswordOnWrongPassword() {
    userService.signUserUp("username", "+73462684906", "test@example.com", "password");
    assertThatThrownBy(() -> userService.login("username", "wrong", session))
        .isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void loginSetsCurrentUserOnCorrectCredentials() {
    userService.signUserUp("username", "+73462684906", "test@example.com", "password");
    userService.login("username", "password", session);
    assertThat(session.getCurrentUserId()).isEqualTo(1);
  }

  @Test
  void editSavesNewUserAndPostsAnEvent() {
    userService.createUser(3, "username", "+73462684906", "test@example.com", "password", UserRole.CLIENT);
    var user = userService.editUser(5, 1, "newUsername", "+5334342", "test1@example.com", "password", UserRole.CLIENT);
    assertThat(users.findById(1)).isEqualTo(user);
    assertThat(events.lookup(Set.of(EventType.USER_MODIFIED), DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .hasSize(1).map(e -> (UserEvent.Edited) e).allMatch(e -> e.getUserId() == 5 && e.getChangedUserId() == 1);
  }

  @Test
  void editThrowsOnNonExistentUser() {
    assertThatThrownBy(() -> userService.editUser(5, 1, "newUsername", "+5334342",
                                                  "test1@example.com", "password", UserRole.CLIENT))
        .isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteUserDeletesUser() {
    userService.signUserUp("username", "+73462684906", "test@example.com", "password");
    userService.deleteUser(5, 1);
  }
}
