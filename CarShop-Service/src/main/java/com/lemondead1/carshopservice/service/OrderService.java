package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.ConflictException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class OrderService {
  private final OrderRepo orders;
  private final CarRepo cars;
  private final TimeService time;

  @Transactional
  @Audited(EventType.ORDER_CREATED)
  public Order createOrder(@Audited.Param("client_id") int clientId,
                           @Audited.Param("car_id") int carId,
                           @Audited.Param("kind") OrderKind kind,
                           @Audited.Param("state") OrderState state,
                           @Audited.Param("comment") String comments) {
    if (kind == OrderKind.PURCHASE && !cars.findById(carId).availableForPurchase()) {
      throw new ForbiddenException("Car #" + carId + " is not available for purchase.");
    }

    //Check if the client has bought the car.
    if (kind == OrderKind.SERVICE && cars.findCarOwner(carId).map(User::id).map(id -> id != clientId).orElse(true)) {
      throw new ForbiddenException("Client #" + clientId + " is not the owner of #" + carId + ".");
    }

    return orders.create(time.now(), kind, state, clientId, carId, comments);
  }

  public Order findById(int orderId) {
    return orders.findById(orderId);
  }

  /**
   * Checks if there are no service orders
   *
   * @param clientId client id
   * @param carId    car id
   */
  private void checkNoServiceOrdersExist(int clientId, int carId) {
    if (orders.doServiceOrdersExistFor(clientId, carId)) {
      throw new CascadingException("Purchase order removal violates service order ownership constraints.");
    }
  }

  /**
   * Deletes the order verifying consistency
   *
   * @param orderId order to be deleted
   */
  @Transactional
  @Audited(EventType.ORDER_DELETED)
  public void deleteOrder(@Audited.Param("order_id") int orderId) {
    var old = orders.findById(orderId);
    if (old.type() == OrderKind.PURCHASE) {
      checkNoServiceOrdersExist(old.client().id(), old.car().id());
    }

    orders.delete(orderId);
  }

  /**
   * Updates order state verifying consistency
   *
   * @param orderId       id of the order
   * @param newState      new state
   * @param appendComment string that is appended to comment
   */
  @Transactional
  @Audited(EventType.ORDER_MODIFIED)
  public Order updateState(@Audited.Param("order_id") int orderId,
                           @Audited.Param("new_state") @Nullable OrderState newState,
                           @Audited.Param("appended_comment") String appendComment) {
    var oldOrder = orders.findById(orderId);

    if (oldOrder.state() == OrderState.DONE && oldOrder.type() == OrderKind.PURCHASE &&
        newState != null && newState != OrderState.DONE) {
      checkNoServiceOrdersExist(oldOrder.client().id(), oldOrder.car().id());
    }

    return orders.edit(orderId, null, null, newState, null, null, oldOrder.comments() + appendComment);
  }

  @Transactional
  @Audited(EventType.ORDER_MODIFIED)
  public Order cancel(@Audited.Param("order_id") int orderId,
                      @Audited.Param("appended_comment") String appendComment) {
    var oldOrder = orders.findById(orderId);
    return switch (oldOrder.state()) {
      case CANCELLED -> throw new ConflictException("This order has already been cancelled.");
      case DONE -> throw new ConflictException("You cannot cancel finished orders.");
      default -> orders.edit(orderId, null, null,
                             OrderState.CANCELLED, null, null,
                             oldOrder.comments() + appendComment);
    };
  }

  @Transactional
  public List<Order> findClientOrders(int userId, OrderSorting sorting) {
    return orders.findClientOrders(userId, sorting);
  }

  @Transactional
  public List<Order> lookupOrders(Range<Instant> dates,
                                  String username,
                                  String carBrand,
                                  String carModel,
                                  Collection<OrderKind> kinds,
                                  Collection<OrderState> states,
                                  OrderSorting sorting) {
    return orders.lookup(dates, username, carBrand, carModel, EnumSet.copyOf(kinds), EnumSet.copyOf(states), sorting);
  }
}
