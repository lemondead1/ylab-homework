package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.util.DateRange;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class OrderService {
  private final OrderRepo orders;
  private final CarRepo cars;
  private final EventService events;
  private final TimeService time;

  public Order purchase(int userId, int carId, String comments) {
    return createOrder(userId, userId, carId, OrderKind.PURCHASE, OrderState.NEW, comments);
  }

  public Order orderService(int userId, int carId, String comments) {
    return createOrder(userId, userId, carId, OrderKind.SERVICE, OrderState.NEW, comments);
  }

  /**
   * Checks for car status and creates an order.
   *
   * @param userId   user that performed the action
   * @param clientId the recipient of the order
   * @param carId    car
   * @param kind     type
   * @param state    initial order state
   * @param comment  comment
   * @return the order created
   */
  public Order createOrder(int userId, int clientId, int carId, OrderKind kind, OrderState state, String comment) {
    //Check if the client has bought the car.
    if (kind == OrderKind.SERVICE && cars.findCarOwner(carId).map(User::id).map(id -> id != clientId).orElse(true)) {
      var message = clientId == userId ? "Car #" + carId + " is not yours."
                                       : "Client #" + clientId + " is not the owner of #" + carId + ".";
      throw new CarReservedException(message);
    }

    if (kind == OrderKind.PURCHASE && !cars.findById(carId).availableForPurchase()) {
      throw new CarReservedException("Car #" + carId + " is not available for purchase.");
    }

    var order = orders.create(time.now(), kind, state, clientId, carId, comment);
    events.onOrderCreated(userId, order);
    return order;
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
   * @param userId  id of the user that performed the action
   * @param orderId order to be deleted
   */
  public void deleteOrder(int userId, int orderId) {
    var old = orders.findById(orderId);
    if (old.type() == OrderKind.PURCHASE) {
      checkNoServiceOrdersExist(old.client().id(), old.car().id());
    }

    orders.delete(orderId);
    events.onOrderDeleted(userId, orderId);
  }

  /**
   * Updates order state verifying consistency
   *
   * @param userId        id of the user that performed the action
   * @param orderId       id of the order
   * @param newState      new state
   * @param appendComment string that is appended to comment
   */
  public void updateState(int userId, int orderId, OrderState newState, String appendComment) {
    var oldOrder = orders.findById(orderId);

    if (oldOrder.state() == OrderState.DONE && oldOrder.type() == OrderKind.PURCHASE && newState != OrderState.DONE) {
      checkNoServiceOrdersExist(oldOrder.client().id(), oldOrder.car().id());
    }

    var newRow = orders.edit(orderId, null, null, newState, null, null, oldOrder.comments() + appendComment);
    events.onOrderEdited(userId, newRow);
  }

  public void cancel(int userId, int orderId) {
    var order = orders.findById(orderId);
    switch (order.state()) {
      case CANCELLED -> throw new CommandException("This order has already been cancelled.");
      case DONE -> throw new CommandException("You cannot cancel finished orders.");
      default -> {
        var newRow = orders.edit(orderId, null, null, OrderState.CANCELLED, null, null, null);
        events.onOrderEdited(userId, newRow);
      }
    }
  }

  public List<Order> findClientOrders(int userId, OrderSorting sorting) {
    return orders.findClientOrders(userId, sorting);
  }

  public List<Order> lookupOrders(DateRange dates,
                                  String username,
                                  String carBrand,
                                  String carModel,
                                  Collection<OrderKind> kinds,
                                  Collection<OrderState> states,
                                  OrderSorting sorting) {
    return orders.lookup(dates, username, carBrand, carModel, EnumSet.copyOf(kinds), EnumSet.copyOf(states), sorting);
  }
}
