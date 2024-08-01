package com.lemondead1.carshopservice.service;

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

  public void onCarEdited(int editorId, int carId, String newBrand, String newModel,
                          int newYearOfIssue, int newPrice, String newCondition) {
    events.submitEvent(new CarEvent.Modified(time.now(), editorId, carId, newBrand, newModel,
                                             newYearOfIssue, newPrice, newCondition));
  }

  public void onCarDeleted(int deleterId, int carId) {
    events.submitEvent(new CarEvent.Deleted(time.now(), deleterId, carId));
  }

  public void onOrderCreated(int creatorId, int orderId, Instant createdAt, OrderKind kind,
                             OrderState state, int customerId, int carId, String comments) {
    events.submitEvent(new OrderEvent.Created(time.now(), creatorId, orderId, createdAt, kind,
                                              state, customerId, carId, comments));
  }

  public void onOrderEdited(int creatorId, int newOrderId, Instant newCreatedAt, OrderKind newKind,
                            OrderState newState, int newCustomerId, int newCarId, String newComments) {
    events.submitEvent(new OrderEvent.Modified(time.now(), creatorId, newOrderId, newCreatedAt, newKind,
                                               newState, newCustomerId, newCarId, newComments));
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
