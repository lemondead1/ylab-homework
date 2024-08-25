package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface OrderService {
  Order createOrder(int clientId, int carId, OrderKind kind, OrderState state, String comments);

  Order findById(int orderId);

  /**
   * Deletes the order verifying consistency
   *
   * @param orderId order to be deleted
   */
  void deleteOrder(int orderId);

  /**
   * Updates order state verifying consistency
   *
   * @param orderId       id of the order
   * @param newState      new state
   * @param appendComment string that is appended to comment
   */
  Order updateState(int orderId, @Nullable OrderState newState, @Nullable String appendComment);

  List<Order> findClientOrders(int userId, OrderSorting sorting);

  List<Order> lookupOrders(Range<Instant> dates,
                           String username,
                           String carBrand,
                           String carModel,
                           Collection<OrderKind> kinds,
                           Collection<OrderState> states,
                           OrderSorting sorting);
}
