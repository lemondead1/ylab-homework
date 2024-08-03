package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.*;
import com.lemondead1.carshopservice.event.CarEvent;
import com.lemondead1.carshopservice.event.OrderEvent;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
  @Mock
  TimeService time;

  CarRepo cars;
  UserRepo users;
  OrderRepo orders;
  EventRepo events;
  EventService service;

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
    service = new EventService(events, time);
  }

  @Test
  void carCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarCreated(2, new Car(1, "BMW", "X5", 2017, 4000000, "mint"));
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new CarEvent.Created(now, 2, 1, "BMW", "X5", 2017, 4000000, "mint"));
  }

  @Test
  void carEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarEdited(4, new Car(2, "BMW", "X5", 2017, 3000000, "good"));
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new CarEvent.Modified(now, 4, 2, "BMW", "X5", 2017, 3000000, "good"));
  }

  @Test
  void carDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onCarDeleted(3, 5);
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.USERNAME_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new CarEvent.Deleted(now, 3, 5));
  }

  @Test
  void orderCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderCreated(6, new Order(10, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                        new User(42, "Uname", "880055535", "test@example.com", "pwd", UserRole.CLIENT),
                                        new Car(64, "Brand", "Model", 1999, 4000000, "nice"),
                                        "No comment"));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .containsExactly(new OrderEvent.Created(now, 6, 10, Instant.EPOCH, OrderKind.PURCHASE,
                                                                        OrderState.NEW, 42, 64, "No comment"));

  }

  @Test
  void orderEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderEdited(6, new Order(10, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                       new User(39, "Uname", "88005553535", "test@example.com", "pwd", UserRole.CLIENT),
                                       new Car(82, "Brand", "Model", 1999, 4000000, "nice"),
                                       "Yes comment"));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .containsExactly(new OrderEvent.Modified(now, 6, 10, Instant.EPOCH, OrderKind.PURCHASE,
                                                                         OrderState.NEW, 39, 82, "Yes comment"));
  }

  @Test
  void orderDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onOrderDeleted(53, 93);
    assertThat(events.listAll())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new OrderEvent.Deleted(now, 53, 93));
  }

  @Test
  void userCreatedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserCreated(43, new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT));
    assertThat(events.listAll()).usingRecursiveFieldByFieldElementComparator()
                                .containsExactly(new UserEvent.Created(now, 43, 71, "alex", "880055535",
                                                                       "test@example.com", UserRole.CLIENT));
  }

  @Test
  void userEditedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserEdited(43,
                         new User(71, "usr", "880055535", "test@example.com", "oldPassword", UserRole.CLIENT),
                         new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT));
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new UserEvent.Edited(now, 43, 71, "alex", "880055535",
                                              "test@example.com", true, UserRole.CLIENT));
  }

  @Test
  void userSignedUpTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserSignedUp(87, "alex");
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new UserEvent.SignUp(now, 87, "alex"));
  }

  @Test
  void userLoggedInTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserLoggedIn(22);
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new UserEvent.Login(now, 22));
  }

  @Test
  void userDeletedTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now);
    service.onUserDeleted(55, 63);
    assertThat(service.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_ASC))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(new UserEvent.Deleted(now, 55, 63));
  }

  @Test
  void dumpTest() {
    var now = Instant.now();
    when(time.now()).thenReturn(now.minusSeconds(100));
    service.onCarCreated(2, new Car(1, "BMW", "X5", 2017, 4000000, "mint"));

    when(time.now()).thenReturn(now.minusSeconds(43));
    service.onCarEdited(4, new Car(2, "BMW", "X5", 2017, 3000000, "good"));

    when(time.now()).thenReturn(now.minusSeconds(213));
    service.onCarDeleted(3, 5);

    when(time.now()).thenReturn(now.minusSeconds(76));
    service.onOrderCreated(6, new Order(10, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                        new User(42, "Uname", "880055535", "test@example.com", "pwd", UserRole.CLIENT),
                                        new Car(64, "Brand", "Model", 1999, 4000000, "nice"),
                                        "No comment"));

    when(time.now()).thenReturn(now.minusSeconds(722));
    service.onOrderEdited(6, new Order(11, Instant.EPOCH, OrderKind.PURCHASE, OrderState.NEW,
                                       new User(39, "Uname", "880055535", "test@example.com", "pwd", UserRole.CLIENT),
                                       new Car(82, "Brand", "Model", 1999, 4000000, "nice"),
                                       "Yes comment"));

    when(time.now()).thenReturn(now.minusSeconds(8432));
    service.onOrderDeleted(53, 93);

    when(time.now()).thenReturn(now.minusSeconds(763));
    service.onUserCreated(43, new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT));

    when(time.now()).thenReturn(now.minusSeconds(754));
    service.onUserEdited(43,
                         new User(71, "usr", "880055535", "test@example.com", "oldPassword", UserRole.CLIENT),
                         new User(71, "alex", "880055535", "test@example.com", "password", UserRole.CLIENT));

    when(time.now()).thenReturn(now.minusSeconds(87));
    service.onUserDeleted(64, 12);

    when(time.now()).thenReturn(now.minusSeconds(9));
    service.onUserSignedUp(87, "alex");

    when(time.now()).thenReturn(now.minusSeconds(12));
    service.onUserLoggedIn(22);

    var expected = String.format("""
                                     {"timestamp": "%s", "type": "car_created", "user_id": 2, "car_id": 1, "brand": "BMW", "model": "X5", "production_year": 2017, "price": 4000000, "condition": "mint"}
                                     {"timestamp": "%s", "type": "car_edited", "user_id": 4, "car_id": 2, "new_brand": "BMW", "new_model": "X5", "new_production_year": 2017, "new_price": 3000000, "new_condition": "good"}
                                     {"timestamp": "%s", "type": "car_deleted", "user_id": 3, "car_id": 5}
                                     {"timestamp": "%s", "type": "order_created", "user_id": 6, "order_id": 10, "created_at": "1970-01-01T00:00:00Z", "kind": "purchase", "state": "new", "customer_id": 42, "car_id": 64, "comments": "No comment"}
                                     {"timestamp": "%s", "type": "order_edited", "user_id": 6, "order_id": 11, "new_created_at": "1970-01-01T00:00:00Z", "new_kind": "purchase", "new_state": "new", "new_customer_id": 39, "new_car_id": 82, "new_comments": "Yes comment"}
                                     {"timestamp": "%s", "type": "order_deleted", "user_id": 53, "order_id": 93}
                                     {"timestamp": "%s", "type": "user_created", "user_id": 43, "created_user_id": 71, "username": "alex", "phone_number": "880055535", "email": "test@example.com", "role": "client"}
                                     {"timestamp": "%s", "type": "user_edited", "user_id": 43, "edited_user_id": 71, "new_username": "alex", "new_phone_number": "880055535", "new_email": "test@example.com", "password_changed": true, "new_role": "client"}
                                     {"timestamp": "%s", "type": "user_deleted", "user_id": 64, "deleted_user_id": 12}
                                     {"timestamp": "%s", "type": "user_logged_in", "user_id": 22}
                                     {"timestamp": "%s", "type": "user_signed_up", "user_id": 87, "username": "alex"}
                                     """,
                                 now.minusSeconds(100),
                                 now.minusSeconds(43),
                                 now.minusSeconds(213),
                                 now.minusSeconds(76),
                                 now.minusSeconds(722),
                                 now.minusSeconds(8432),
                                 now.minusSeconds(763),
                                 now.minusSeconds(754),
                                 now.minusSeconds(87),
                                 now.minusSeconds(12),
                                 now.minusSeconds(9));

    var stringWriter = new StringWriter();
    service.dumpEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TYPE_ASC, stringWriter);
    assertThat(stringWriter.toString()).isEqualTo(expected);
  }
}
