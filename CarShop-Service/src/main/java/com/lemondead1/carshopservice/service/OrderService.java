package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.ConflictException;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.exceptions.NotFoundException;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface OrderService {
  /**
   * Creates a new order.
   *
   * @return Created order.
   * @throws NotFoundException if a user with id {@code clientId} or a car with id {@code carId} does not exist.
   * @throws ConflictException if {@code kind} is {@linkplain OrderKind#PURCHASE} the car is not available for purchase or {@code kind} is {@linkplain OrderKind#SERVICE} and user #{@code clientId} does not own the car.
   */
  Order createOrder(int clientId, int carId, OrderKind kind, OrderState state, String comments);

  /**
   * Looks up an order by its id.
   *
   * @throws NotFoundException if an order with the given id does not exist.
   */
  Order findById(int orderId);

  /**
   * Deletes the order verifying consistency
   *
   * @param orderId order to be deleted
   * @throws NotFoundException if an order with the given id does not exist.
   */
  void deleteOrder(int orderId);

  /**
   * Updates order state verifying consistency
   *
   * @param orderId       id of the order
   * @param newState      new state
   * @param appendComment string that is appended to comment
   * @throws NotFoundException if an order with the given id does not exist.
   */
  Order updateState(int orderId, @Nullable OrderState newState, @Nullable String appendComment);

  /**
   * Looks up orders with the matching {@code clientId}.
   *
   * @param clientId User id.
   * @param sorting  Order sorting.
   * @return List of order with the matching {@code clientId}.
   */
  List<Order> findClientOrders(int clientId, OrderSorting sorting);

  /**
   * Searches for orders matching arguments.
   *
   * @param dates    Order creation date range.
   * @param username Username query.
   * @param carBrand Car brand query.
   * @param carModel Car model query.
   * @param kinds    Order kind filter.
   * @param states   Order state filter.
   * @param sorting  Order sorting.
   * @return List of orders matching arguments.
   */
  List<Order> lookupOrders(Range<Instant> dates,
                           String username,
                           String carBrand,
                           String carModel,
                           Collection<OrderKind> kinds,
                           Collection<OrderState> states,
                           OrderSorting sorting);
}
