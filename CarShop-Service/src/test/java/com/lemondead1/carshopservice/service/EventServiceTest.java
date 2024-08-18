package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.EventRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
  @Mock
  TimeService time;

  @Mock
  EventRepo events;

  @InjectMocks
  EventService service;

  @Test
  void postEvent() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.postEvent(5, EventType.USER_CREATED, Map.of("test", "string"));
    verify(events).create(now, 5, EventType.USER_CREATED, Map.of("test", "string"));
  }

  @Test
  void userSignedUpTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserSignedUp(new User(71, "usr", "880055535", "test@example.com", "oldPassword", UserRole.CLIENT, 0));
    verify(events).create(eq(now), eq(71), eq(EventType.USER_SIGNED_UP), any());
  }

  @Test
  void userLoggedInTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserLoggedIn(22);
    verify(events).create(eq(now), eq(22), eq(EventType.USER_LOGGED_IN), any());
  }
}
