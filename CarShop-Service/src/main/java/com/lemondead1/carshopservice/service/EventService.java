package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.event.CarEvent;
import com.lemondead1.carshopservice.event.OrderEvent;
import com.lemondead1.carshopservice.event.UserEvent;
import com.lemondead1.carshopservice.exceptions.DumpException;
import com.lemondead1.carshopservice.repo.EventRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class EventService {
  private final EventRepo events;
  private final TimeService time;

  public EventService(EventRepo events, TimeService time) {
    this.events = events;
    this.time = time;
  }

  public void onCarCreated(int creatorId, int carId, String brand, String model,
                           int yearOfIssue, int price, String condition) {
    events.submitEvent(new CarEvent.Created(time.now(), creatorId, carId, brand, model, yearOfIssue, price, condition));
  }

  public void onCarEdited(int editorId, Car newCar) {
    events.submitEvent(new CarEvent.Modified(time.now(), editorId, newCar.id(), newCar.brand(),
                                             newCar.model(), newCar.yearOfIssue(), newCar.price(), newCar.condition()));
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
