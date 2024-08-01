package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.PurchaseOrderState;

import java.time.Instant;
import java.util.Objects;

public abstract class PurchaseOrderEvent extends Event {
  private final int orderId;

  PurchaseOrderEvent(Instant timestamp, int userId, int orderId) {
    super(timestamp, userId);
    this.orderId = orderId;
  }

  public int getOrderId() {
    return orderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    if (!super.equals(o)) { return false; }

    PurchaseOrderEvent that = (PurchaseOrderEvent) o;
    return orderId == that.orderId;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + orderId;
    return result;
  }

  public static class Created extends PurchaseOrderEvent {
    private final Instant createdAt;
    private final PurchaseOrderState state;
    private final int customerId;
    private final int carId;

    public Created(Instant timestamp, int userId, int orderId, Instant createdAt,
                   PurchaseOrderState state, int customerId, int carId) {
      super(timestamp, userId, orderId);
      Objects.requireNonNull(createdAt);
      Objects.requireNonNull(state);
      this.createdAt = createdAt;
      this.state = state;
      this.customerId = customerId;
      this.carId = carId;
    }

    public Instant getCreatedAt() {
      return createdAt;
    }

    public PurchaseOrderState getState() {
      return state;
    }

    public int getCustomerId() {
      return customerId;
    }

    public int getCarId() {
      return carId;
    }

    @Override
    public EventType getType() {
      return EventType.PURCHASE_ORDER_CREATED;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }

      Created created = (Created) o;
      return customerId == created.customerId && carId == created.carId && createdAt.equals(created.createdAt) &&
             state == created.state;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + createdAt.hashCode();
      result = 31 * result + state.hashCode();
      result = 31 * result + customerId;
      result = 31 * result + carId;
      return result;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "created_at": "%s", "state": "%s", "customer_id": %d, "car_id": %d}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(), getCreatedAt(),
                           getState().getId(), getCustomerId(), getCarId());
    }
  }

  public static class Modified extends PurchaseOrderEvent {
    private final Instant newCreatedAt;
    private final PurchaseOrderState newState;
    private final int newCustomerId;
    private final int newCarId;

    public Modified(Instant timestamp, int userId, int orderId, Instant newCreatedAt,
                    PurchaseOrderState newState, int newCustomerId, int newCarId) {
      super(timestamp, userId, orderId);
      Objects.requireNonNull(newCreatedAt);
      Objects.requireNonNull(newState);
      this.newCreatedAt = newCreatedAt;
      this.newState = newState;
      this.newCustomerId = newCustomerId;
      this.newCarId = newCarId;
    }

    public Instant getNewCreatedAt() {
      return newCreatedAt;
    }

    public PurchaseOrderState getNewState() {
      return newState;
    }

    public int getNewCustomerId() {
      return newCustomerId;
    }

    public int getNewCarId() {
      return newCarId;
    }

    @Override
    public EventType getType() {
      return EventType.PURCHASE_ORDER_MODIFIED;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      if (!super.equals(o)) { return false; }

      Modified modified = (Modified) o;
      return newCustomerId == modified.newCustomerId && newCarId == modified.newCarId &&
             newCreatedAt.equals(modified.newCreatedAt) && newState == modified.newState;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + newCreatedAt.hashCode();
      result = 31 * result + newState.hashCode();
      result = 31 * result + newCustomerId;
      result = 31 * result + newCarId;
      return result;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "new_created_at": "%s", "new_state": "%s", "new_customer_id": %d, "new_car_id": %d}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(), getNewCreatedAt(),
                           getNewState().getId(), getNewCustomerId(), getNewCarId());
    }
  }

  public static class Deleted extends PurchaseOrderEvent {
    public Deleted(Instant timestamp, int userId, int orderId) {
      super(timestamp, userId, orderId);
    }

    @Override
    public EventType getType() {
      return EventType.PURCHASE_ORDER_DELETED;
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
