package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.aspect.AuditedAspect;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
  private static final UserRepo users = new UserRepo(TestDBConnector.DB_MANAGER);

  @Mock
  EventService eventService;

  SessionService session;

  private final User dummyUser = new User(5, "dummy", "123456789", "dummy@example.com", "password", UserRole.ADMIN, 0);

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
    Aspects.aspectOf(AuditedAspect.class).setCurrentUserProvider(() -> dummyUser);
    Aspects.aspectOf(AuditedAspect.class).setEventService(eventService);
    session = new SessionService(users, eventService);
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
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
