package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.WrongUsernamePasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
  @Mock
  UserService users;

  @Mock
  EventService eventService;

  SessionService session;

  @BeforeEach
  void setup() {
    session = new SessionService(users, eventService);
  }

  @Test
  void loginThrowsOnWrongUsername() {
    when(users.findByUsername("wrongUsername")).thenThrow(new RowNotFoundException());

    assertThatThrownBy(() -> session.login("wrongUsername", "pass")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void loginThrowsOnWrongPassword() {
    var dummyUser = new User(1, "testUsername", "88005553535", "test@example.com", "pass", UserRole.CLIENT, 0);
    when(users.findByUsername("testUsername")).thenReturn(dummyUser);

    assertThatThrownBy(() -> session.login("testUsername", "wrong")).isInstanceOf(WrongUsernamePasswordException.class);
  }

  @Test
  void successfulLoginTest() {
    var dummyUser = new User(1, "testUsername", "88005553535", "test@example.com", "pass", UserRole.CLIENT, 0);
    when(users.findByUsername("testUsername")).thenReturn(dummyUser);
    when(users.findById(1)).thenReturn(dummyUser);

    session.login("testUsername", "pass");

    assertThat(session.getCurrentUser()).isEqualTo(dummyUser);
    verify(eventService).onUserLoggedIn(1);
  }

  @Test
  void logoutTest() {
    var dummyUser = new User(1, "testUsername", "88005553535", "test@example.com", "pass", UserRole.CLIENT, 0);
    when(users.findByUsername("testUsername")).thenReturn(dummyUser);

    session.login("testUsername", "pass");
    session.logout();

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }

  @Test
  void deletedUserTest() {
    var dummyUser = new User(1, "testUsername", "88005553535", "test@example.com", "pass", UserRole.CLIENT, 0);
    when(users.findByUsername("testUsername")).thenReturn(dummyUser);
    when(users.findById(1)).thenThrow(new RowNotFoundException());

    session.login("testUsername", "pass");

    assertThat(session.getCurrentUser()).matches(u -> u.id() == 0 && u.role() == UserRole.ANONYMOUS);
  }
}
