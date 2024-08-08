package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.event.CarEvent;
import com.lemondead1.carshopservice.event.Event;
import com.lemondead1.carshopservice.event.OrderEvent;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.exceptions.DumpException;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.util.DateRange;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * This class is responsible for interfacing with the event database.
 * It provides convenience methods for both submitting and querying events.
 */
public class EventService {
  private final EventRepo events;
  private final TimeService time;

  public EventService(EventRepo events, TimeService time) {
    this.events = events;
    this.time = time;
  }

  public void onCarCreated(int creatorId, Car car) {
    events.submitEvent(new CarEvent.Created(time.now(), creatorId, car.id(), car.brand(), car.model(),
                                            car.productionYear(), car.price(), car.condition()));
  }

  public void onCarEdited(int editorId, Car newCar) {
    events.submitEvent(new CarEvent.Modified(time.now(), editorId, newCar.id(), newCar.brand(),
                                             newCar.model(), newCar.productionYear(), newCar.price(),
                                             newCar.condition()));
  }

  public void onCarDeleted(int deleterId, int carId) {
    events.submitEvent(new CarEvent.Deleted(time.now(), deleterId, carId));
  }

  public void onOrderCreated(int creatorId, Order order) {
    events.submitEvent(new OrderEvent.Created(time.now(), creatorId, order.id(), order.createdAt(), order.type(),
                                              order.state(), order.customer().id(), order.car().id(),
                                              order.comments()));
  }

  public void onOrderEdited(int editorId, Order newOrder) {
    events.submitEvent(new OrderEvent.Modified(time.now(), editorId, newOrder.id(), newOrder.createdAt(),
                                               newOrder.type(), newOrder.state(), newOrder.customer().id(),
                                               newOrder.car().id(), newOrder.comments()));
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
    events.submitEvent(
        new UserEvent.Edited(time.now(), editorId, newUser.id(), newUser.username(), newUser.phoneNumber(),
                             newUser.email(), !oldUser.password().equals(newUser.password()), newUser.role()));
  }

  public void onUserCreated(int creatorId, User created) {
    events.submitEvent(new UserEvent.Created(time.now(), creatorId, created.id(), created.username(),
                                             created.phoneNumber(), created.email(), created.role()));
  }

  public void onUserDeleted(int deleterId, int userId) {
    events.submitEvent(new UserEvent.Deleted(time.now(), deleterId, userId));
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
        writer.write(ev.serialize());
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
