package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.service.impl.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
  @Mock
  EventRepo events;

  @InjectMocks
  EventServiceImpl service;

  @Test
  void postEvent() {
    Instant now = Instant.now();

    var currentRequest = new MockHttpServletRequest();
    currentRequest.setUserPrincipal(new User(5, "admin", "88005553535", "admin@ya.com", "password", UserRole.ADMIN, 0));
    RequestContextHolder.setRequestAttributes(new ServletWebRequest(currentRequest));

    try (MockedStatic<Instant> mocked = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
      mocked.when(Instant::now).thenReturn(now);
      service.postEvent("user_created", Map.of("test", "string"));
      verify(events).create(now, 5, EventType.USER_CREATED, Map.of("test", "string"));
    }
  }

  @Test
  void userSignedUpTest() {
    Instant now = Instant.now();
    try (MockedStatic<Instant> mocked = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
      mocked.when(Instant::now).thenReturn(now);
      service.onUserSignedUp(new User(71, "usr", "880055535", "test@example.com", "oldPassword", UserRole.CLIENT, 0));
      verify(events).create(eq(now), eq(71), eq(EventType.USER_SIGNED_UP), any());
    }
  }

  @Test
  void userLoggedInTest() {
    Instant now = Instant.now();
    try (MockedStatic<Instant> mocked = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
      mocked.when(Instant::now).thenReturn(now);
      service.onUserLoggedIn(22);
      verify(events).create(eq(now), eq(22), eq(EventType.USER_LOGGED_IN), any());
    }
  }
}
