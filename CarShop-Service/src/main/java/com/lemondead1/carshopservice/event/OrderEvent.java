package com.lemondead1.carshopservice.event;

import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.util.JsonUtil;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public abstract class OrderEvent extends Event {
  private final int orderId;

  OrderEvent(Instant timestamp, int userId, int orderId) {
    super(timestamp, userId);
    this.orderId = orderId;
  }

  @Getter
  public static class Created extends OrderEvent {
    private final Instant createdAt;
    private final OrderKind kind;
    private final OrderState state;
    private final int customerId;
    private final int carId;
    private final String comments;

    public Created(Instant timestamp, int userId, int orderId, Instant createdAt, OrderKind kind,
                   OrderState state, int customerId, int carId, String comments) {
      super(timestamp, userId, orderId);
      this.kind = Objects.requireNonNull(kind);
      this.comments = Objects.requireNonNull(comments);
      this.createdAt = Objects.requireNonNull(createdAt);
      this.state = Objects.requireNonNull(state);
      this.customerId = customerId;
      this.carId = carId;
    }

    @Override
    public EventType getType() {
      return EventType.ORDER_CREATED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "created_at": "%s", "kind": "%s", "state": "%s", "customer_id": %d, "car_id": %d, "comments": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(), getCreatedAt(),
                           getKind(), getState().getId(), getCustomerId(), getCarId());
    }
  }

  @Getter
  public static class Modified extends OrderEvent {
    private final Instant newCreatedAt;
    private final OrderKind newKind;
    private final OrderState newState;
    private final int newCustomerId;
    private final int newCarId;
    private final String newComments;

    public Modified(Instant timestamp, int userId, int orderId, Instant newCreatedAt, OrderKind newKind,
                    OrderState newState, int newCustomerId, int newCarId, String newComments) {
      super(timestamp, userId, orderId);
      this.newCreatedAt = Objects.requireNonNull(newCreatedAt);
      this.newKind = Objects.requireNonNull(newKind);
      this.newState = Objects.requireNonNull(newState);
      this.newCustomerId = newCustomerId;
      this.newCarId = newCarId;
      this.newComments = Objects.requireNonNull(newComments);
    }

    @Override
    public EventType getType() {
      return EventType.ORDER_MODIFIED;
    }

    @Override
    public String serialize() {
      var pattern = """
          {"timestamp": "%s", "type": "%s", "user_id": %d, "order_id": %d, "new_created_at": "%s", "new_kind": "%s", "new_state": "%s", "new_customer_id": %d, "new_car_id": %d, "new_comments": "%s"}
          """;
      return String.format(pattern, getTimestamp(), getType().getId(), getUserId(), getOrderId(), getNewCreatedAt(),
                           getNewKind().getId(), getNewState().getId(), getNewCustomerId(), getNewCarId(),
                           JsonUtil.escapeCharacters(getNewComments()));
    }
  }

  public static class Deleted extends OrderEvent {
    public Deleted(Instant timestamp, int userId, int orderId) {
      super(timestamp, userId, orderId);
    }

    @Override
    public EventType getType() {
      return EventType.ORDER_DELETED;
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
