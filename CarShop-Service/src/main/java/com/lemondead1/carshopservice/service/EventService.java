package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.event.*;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.enums.PurchaseOrderState;
import com.lemondead1.carshopservice.enums.ServiceOrderState;
import com.lemondead1.carshopservice.exceptions.DumpException;

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

  public void onPurchaseOrderCreated(int creatorId, int orderId, Instant createdAt,
                                     PurchaseOrderState state, int customerId, int carId) {
    events.submitEvent(new PurchaseOrderEvent.Created(time.now(), creatorId, orderId, createdAt,
                                                      state, customerId, carId));
  }

  public void onPurchaseOrderEdited(int creatorId, int newOrderId, Instant newCreatedAt,
                                    PurchaseOrderState newOrderState, int newCustomerId, int newCarId) {
    events.submitEvent(new PurchaseOrderEvent.Modified(time.now(), creatorId, newOrderId, newCreatedAt,
                                                       newOrderState, newCustomerId, newCarId));
  }

  public void onPurchaseOrderDeleted(int deleterId, int orderId) {
    events.submitEvent(new PurchaseOrderEvent.Deleted(time.now(), deleterId, orderId));
  }

  public void onServiceOrderCreated(int creatorId, int orderId, Instant createdAt, ServiceOrderState state,
                                    int customerId, int carId, String complaints) {
    events.submitEvent(new ServiceOrderEvent.Created(time.now(), creatorId, orderId, createdAt,
                                                     state, customerId, carId, complaints));
  }

  public void onServiceOrderEdited(int editorId, int newOrderId, Instant newCreatedAt, ServiceOrderState newOrderState,
                                   int newCustomerId, int newCarId, String newComplaints) {
    events.submitEvent(new ServiceOrderEvent.Modified(time.now(), editorId, newOrderId, newCreatedAt,
                                                      newOrderState, newCustomerId, newCarId, newComplaints));
  }

  public void onServiceOrderDeleted(int deleterId, int orderId) {
    events.submitEvent(new ServiceOrderEvent.Deleted(time.now(), deleterId, orderId));
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
