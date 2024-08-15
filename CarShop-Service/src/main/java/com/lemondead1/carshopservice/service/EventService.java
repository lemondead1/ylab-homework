package com.lemondead1.carshopservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.DumpException;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.JsonUtil;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class is responsible for interfacing with the event database.
 * It provides convenience methods for both submitting and querying events.
 */
@RequiredArgsConstructor
public class EventService {
  private final ObjectMapper objectMapper;
  private final EventRepo events;
  private final TimeService time;

  public void postEvent(int userId, EventType eventType, Map<String, Object> data) {
    var now = time.now();
    var dataCopy = new LinkedHashMap<>(data);
    dataCopy.put("type", eventType);
    dataCopy.put("userId", userId);
    String json;
    try {
      json = objectMapper.writeValueAsString(dataCopy);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    events.create(now, userId, eventType, json);
  }

  public void onCarCreated(int userId, Car car) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.CAR_CREATED)
                       .append("user_id", userId)
                       .append("car_id", car.id())
                       .append("brand", car.brand())
                       .append("model", car.model())
                       .append("production_year", car.productionYear())
                       .append("price", car.price())
                       .append("condition", car.condition()).build();
    events.create(now, userId, EventType.CAR_CREATED, json);
  }

  public void onCarEdited(int userId, Car newCar) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.CAR_MODIFIED)
                       .append("user_id", userId)
                       .append("car_id", newCar.id())
                       .append("new_brand", newCar.brand())
                       .append("new_model", newCar.model())
                       .append("new_production_year", newCar.productionYear())
                       .append("new_price", newCar.price())
                       .append("new_condition", newCar.condition()).build();
    events.create(now, userId, EventType.CAR_MODIFIED, json);
  }

  public void onCarDeleted(int userId, int carId) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.CAR_DELETED)
                       .append("user_id", userId)
                       .append("car_id", carId).build();
    events.create(now, userId, EventType.CAR_DELETED, json);
  }

  public void onOrderCreated(int userId, Order order) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.ORDER_CREATED)
                       .append("user_id", userId)
                       .append("order_id", order.id())
                       .append("created_at", order.createdAt())
                       .append("order_type", order.type())
                       .append("state", order.state())
                       .append("client_id", order.client().id())
                       .append("car_id", order.car().id())
                       .append("comments", order.comments()).build();
    events.create(now, userId, EventType.ORDER_CREATED, json);
  }

  public void onOrderEdited(int userId, Order newOrder) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.ORDER_MODIFIED)
                       .append("user_id", userId)
                       .append("order_id", newOrder.id())
                       .append("new_created_at", newOrder.createdAt())
                       .append("new_order_type", newOrder.type())
                       .append("new_state", newOrder.state())
                       .append("new_client_id", newOrder.client().id())
                       .append("new_car_id", newOrder.car().id())
                       .append("new_comments", newOrder.comments()).build();
    events.create(now, userId, EventType.ORDER_MODIFIED, json);
  }

  public void onOrderDeleted(int userId, int orderId) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.ORDER_DELETED)
                       .append("user_id", userId)
                       .append("order_id", orderId).build();
    events.create(now, userId, EventType.ORDER_DELETED, json);
  }

  public void onUserLoggedIn(int userId) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.USER_LOGGED_IN)
                       .append("user_id", userId).build();
    events.create(now, userId, EventType.USER_LOGGED_IN, json);
  }

  public void onUserSignedUp(User user) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.USER_SIGNED_UP)
                       .append("user_id", user.id())
                       .append("username", user.username())
                       .append("phone_number", user.phoneNumber())
                       .append("email", user.email()).build();
    events.create(now, user.id(), EventType.USER_SIGNED_UP, json);
  }

  public void onUserEdited(int userId, User oldUser, User newUser) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.USER_MODIFIED)
                       .append("user_id", userId)
                       .append("edited_user_id", newUser.id())
                       .append("new_username", newUser.username())
                       .append("new_phone_number", newUser.phoneNumber())
                       .append("new_email", newUser.email())
                       .append("password_changed", !oldUser.password().equals(newUser.password()))
                       .append("new_role", newUser.role()).build();
    events.create(now, userId, EventType.USER_MODIFIED, json);
  }

  public void onUserCreated(int userId, User user) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.USER_CREATED)
                       .append("user_id", userId)
                       .append("created_user_id", user.id())
                       .append("username", user.username())
                       .append("phone_number", user.phoneNumber())
                       .append("email", user.email())
                       .append("role", user.role()).build();
    events.create(now, userId, EventType.USER_CREATED, json);
  }

  public void onUserDeleted(int userId, int deletedUserId) {
    var now = time.now();
    var json = JsonUtil.jsonBuilder()
                       .append("timestamp", now)
                       .append("type", EventType.USER_DELETED)
                       .append("user_id", userId)
                       .append("deleted_user_id", deletedUserId).build();
    events.create(now, userId, EventType.USER_DELETED, json);
  }

  public List<Event> findEvents(Collection<EventType> types, DateRange range, String username, EventSorting sorting) {
    return events.lookup(EnumSet.copyOf(types), range, username, sorting);
  }

  /**
   * Writes matching events into writer in JSON line format.
   *
   * @param types    The types of events to dump
   * @param range    Time range for events
   * @param username Username search query
   * @param sorting  Event sorting
   * @param writer   Writer for events
   * @return The list of events dumped
   * @throws DumpException when an IOException occurs
   */
  public List<Event> dumpEvents(Collection<EventType> types, DateRange range, String username, EventSorting sorting,
                                Writer writer) {
    var list = findEvents(types, range, username, sorting);
    try {
      for (var ev : list) {
        writer.write(ev.json());
        writer.write('\n');
      }
    } catch (IOException e) {
      throw new DumpException("Failed to create an event dump", e);
    }
    return list;
  }

  /**
   * Dumps events into a file.
   * {@link #dumpEvents(Collection, DateRange, String, EventSorting, Writer)}
   */
  public List<Event> dumpEvents(Collection<EventType> types, DateRange range, String username, EventSorting sorting,
                                Path file) {
    try (var writer = Files.newBufferedWriter(file)) {
      return dumpEvents(types, range, username, sorting, writer);
    } catch (IOException e) {
      throw new DumpException("Failed to create an event dump", e);
    }
  }
}
