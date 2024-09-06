package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.DBInitializer;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(initializers = DBInitializer.class)
public class LoginServiceTest {
  @MockBean
  EventServiceImpl eventService;

  @Autowired
  UserRepo users;

  @Autowired
  SignupLoginService session;

  @BeforeEach
  void beforeEach() {
    var currentRequest = new MockHttpServletRequest();
    currentRequest.setUserPrincipal(new User(1, "admin", "88005553535", "admin@ya.com", "password", UserRole.ADMIN, 0));
    RequestContextHolder.setRequestAttributes(new ServletWebRequest(currentRequest));
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
