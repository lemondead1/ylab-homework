package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.event.CarEvent;
import com.lemondead1.carshopservice.event.Event;
import com.lemondead1.carshopservice.event.OrderEvent;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.exceptions.DumpException;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.util.DateRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class EventService {
  private final EventRepo events;
  private final TimeService time;

  public EventService(EventRepo events, TimeService time) {
    this.events = events;
    this.time = time;
  }

  public void onCarCreated(int creatorId, int carId, String brand, String model,
                           int productionYear, int price, String condition) {
    events.submitEvent(
        new CarEvent.Created(time.now(), creatorId, carId, brand, model, productionYear, price, condition));
  }

  public void onCarEdited(int editorId, Car newCar) {
    events.submitEvent(new CarEvent.Modified(time.now(), editorId, newCar.id(), newCar.brand(),
                                             newCar.model(), newCar.productionYear(), newCar.price(),
                                             newCar.condition()));
  }

  public void onCarDeleted(int deleterId, int carId) {
    events.submitEvent(new CarEvent.Deleted(time.now(), deleterId, carId));
  }

  public void onOrderCreated(int creatorId, int orderId, Instant createdAt, OrderKind kind,
                             OrderState state, int customerId, int carId, String comments) {
    events.submitEvent(new OrderEvent.Created(time.now(), creatorId, orderId, createdAt, kind,
                                              state, customerId, carId, comments));
  }

  public void onOrderEdited(int editorId, Order order) {
    events.submitEvent(new OrderEvent.Modified(time.now(), editorId, order.id(), order.createdAt(), order.type(),
                                               order.state(), order.customer().id(), order.car().id(),
                                               order.comments()));
  }

  public void onOrderDeleted(int deleterId, int orderId) {
    events.submitEvent(new OrderEvent.Deleted(time.now(), deleterId, orderId));
  }

  public void onUserLoggedIn(int userId) {
    events.submitEvent(new UserEvent.Login(time.now(), userId));
  }

  public void onUserSignedUp(int userId, String username) {
    events.submitEvent(new UserEvent.SignUp(time.now(), userId, username));
  }

  public void onUserEdited(int editorId, User oldUser, User newUser) {
    events.submitEvent(new UserEvent.Edited(time.now(), editorId, newUser.id(), newUser.username(),
                                            !oldUser.password().equals(newUser.password())));
  }

  public void onUserCreated(int creatorId, User created) {
    events.submitEvent(new UserEvent.Created(time.now(), creatorId, created.id(), created.username()));
  }

  public List<Event> findEvents(Collection<EventType> types, DateRange range, String username, EventSorting sorting) {
    return events.lookupEvents(EnumSet.copyOf(types), range, username, sorting);
  }

  public List<Event> dumpEvents(Collection<EventType> types, DateRange range, String username, EventSorting sorting,
                                Path file) {
    var list = events.lookupEvents(EnumSet.copyOf(types), range, username, sorting);
    try (var writer = Files.newBufferedWriter(file)) {
      for (var ev : list) {
        writer.write(ev.serialize());
        writer.write('\n');
      }
    } catch (IOException e) {
      throw new DumpException("Failed to create an event dump", e);
    }
    return list;
  }

  public void dumpAll(Path file) {
    try (var writer = Files.newBufferedWriter(file)) {
      for (var ev : events.listEvents()) {
        writer.write(ev.serialize());
      }
    } catch (IOException e) {
      throw new DumpException("Failed to create an event dump", e);
    }
  }
}
