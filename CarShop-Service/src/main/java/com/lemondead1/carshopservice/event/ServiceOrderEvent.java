package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.ServiceOrderState;

import java.time.Instant;
import java.util.Objects;

public abstract class ServiceOrderEvent extends Event {
  private final int orderId;

  ServiceOrderEvent(Instant timestamp, int userId, int orderId) {
    super(timestamp, userId);
    this.orderId = orderId;
  }

  public int getOrderId() {
    return orderId;
  }

  public static class Created extends ServiceOrderEvent {
    private final Instant createdAt;
    private final ServiceOrderState state;
    private final int customerId;
    private final int carId;
    private final String complaints;

    public Created(Instant timestamp, int userId, int orderId, Instant createdAt,
                   ServiceOrderState state, int customerId, int carId, String complaints) {
      super(timestamp, userId, orderId);
      Objects.requireNonNull(createdAt);
      Objects.requireNonNull(state);
      Objects.requireNonNull(complaints);
      this.createdAt = createdAt;
      this.state = state;
      this.customerId = customerId;
      this.carId = carId;
      this.complaints = complaints;
    }

    public Instant getCreatedAt() {
      return createdAt;
    }

    public ServiceOrderState getState() {
      return state;
    }

    public int getCustomerId() {
      return customerId;
    }

    public int getCarId() {
      return carId;
    }

    public String getComplaints() {
      return complaints;
    }

    @Override
    public EventType getType() {
      return EventType.SERVICE_ORDER_CREATED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "created_at": "%s", "state": "%s", "customer_id": %d, "car_id": %d, "complaints": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(), getCreatedAt(),
                           getState().getId(), getCustomerId(), getCarId(), getComplaints());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }

      Created created = (Created) o;
      return customerId == created.customerId && carId == created.carId && createdAt.equals(created.createdAt) &&
             state == created.state && complaints.equals(created.complaints);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + createdAt.hashCode();
      result = 31 * result + state.hashCode();
      result = 31 * result + customerId;
      result = 31 * result + carId;
      result = 31 * result + complaints.hashCode();
      return result;
    }
  }

  public static class Modified extends ServiceOrderEvent {
    private final Instant newCreatedAt;
    private final ServiceOrderState newState;
    private final int newCustomerId;
    private final int newCarId;
    private final String newComplaints;

    public Modified(Instant timestamp, int userId, int orderId, Instant newCreatedAt,
                    ServiceOrderState newState, int newCustomerId, int newCarId, String newComplaints) {
      super(timestamp, userId, orderId);
      Objects.requireNonNull(newCreatedAt);
      Objects.requireNonNull(newState);
      Objects.requireNonNull(newComplaints);
      this.newCreatedAt = newCreatedAt;
      this.newState = newState;
      this.newCustomerId = newCustomerId;
      this.newCarId = newCarId;
      this.newComplaints = newComplaints;
    }

    public Instant getNewCreatedAt() {
      return newCreatedAt;
    }

    public ServiceOrderState getNewState() {
      return newState;
    }

    public int getNewCustomerId() {
      return newCustomerId;
    }

    public int getNewCarId() {
      return newCarId;
    }

    public String getNewComplaints() {
      return newComplaints;
    }

    @Override
    public EventType getType() {
      return EventType.SERVICE_ORDER_MODIFIED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "new_created_at": "%s", "new_state": "%s", "new_customer_id": %d, "new_car_id": %d, "new_complaints": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(),
                           getNewCreatedAt(), getNewState().getId(), getNewCustomerId(), getNewCarId(),
                           getNewComplaints());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }

      Modified modified = (Modified) o;
      return newCustomerId == modified.newCustomerId && newCarId == modified.newCarId &&
             newCreatedAt.equals(modified.newCreatedAt) && newState == modified.newState &&
             newComplaints.equals(modified.newComplaints);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + newCreatedAt.hashCode();
      result = 31 * result + newState.hashCode();
      result = 31 * result + newCustomerId;
      result = 31 * result + newCarId;
      result = 31 * result + newComplaints.hashCode();
      return result;
    }
  }

  public static class Deleted extends ServiceOrderEvent {
    public Deleted(Instant timestamp, int userId, int orderId) {
      super(timestamp, userId, orderId);
    }

    @Override
    public EventType getType() {
      return EventType.SERVICE_ORDER_DELETED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId());
    }
  }
}
