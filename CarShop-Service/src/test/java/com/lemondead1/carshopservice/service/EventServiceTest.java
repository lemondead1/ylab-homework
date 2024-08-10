package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.repo.EventRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

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
  void carCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarCreated(2, new Car(1, "BMW", "X5", 2017, 4000000, "mint", true));
    verify(events).create(eq(now), eq(2), eq(EventType.CAR_CREATED), any());
  }

  @Test
  void carEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarEdited(4, new Car(2, "BMW", "X5", 2017, 3000000, "good", true));
    verify(events).create(eq(now), eq(4), eq(EventType.CAR_MODIFIED), any());
  }

  @Test
  void carDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarDeleted(3, 5);
    verify(events).create(eq(now), eq(3), eq(EventType.CAR_DELETED), any());
  }

  @Test
  void orderCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderCreated(6, new Order(10, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                        new User(42, "Uname", "880055535", "test@ya.com", "pwd", UserRole.CLIENT, 0),
                                        new Car(64, "Brand", "Model", 1999, 4000000, "nice", true),
                                        "No comment"));
    verify(events).create(eq(now), eq(6), eq(EventType.ORDER_CREATED), any());
  }

  @Test
  void orderEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderEdited(6, new Order(10, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                       new User(39, "Uname", "88005553535", "test@ya.com", "pwd", UserRole.CLIENT, 0),
                                       new Car(82, "Brand", "Model", 1999, 4000000, "nice", true),
                                       "Yes comment"));
    verify(events).create(eq(now), eq(6), eq(EventType.ORDER_MODIFIED), any());
  }

  @Test
  void orderDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderDeleted(53, 93);
    verify(events).create(eq(now), eq(53), eq(EventType.ORDER_DELETED), any());
  }

  @Test
  void userCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserCreated(43, new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT, 0));
    verify(events).create(eq(now), eq(43), eq(EventType.USER_CREATED), any());
  }

  @Test
  void userEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserEdited(43,
                         new User(71, "usr", "880055535", "test@example.com", "oldPassword", UserRole.CLIENT, 0),
                         new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT, 0));
    verify(events).create(eq(now), eq(43), eq(EventType.USER_MODIFIED), any());
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

  @Test
  void userDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserDeleted(55, 63);
    verify(events).create(eq(now), eq(55), eq(EventType.USER_DELETED), any());
  }
}
