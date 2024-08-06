package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.repo.OrderRepo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class OrderService {
  private final OrderRepo orders;
  private final EventService events;
  private final TimeService time;

  public OrderService(OrderRepo orders, EventService events, TimeService time) {
    this.orders = orders;
    this.events = events;
    this.time = time;
  }

  public Car purchase(int user, int carId, String comments) {
    return createOrder(user, user, carId, OrderKind.PURCHASE, OrderState.NEW, comments).car();
  }

  public Car orderService(int user, int carId, String comments) {
    return createOrder(user, user, carId, OrderKind.SERVICE, OrderState.NEW, comments).car();
  }

  /**
   * Checks for car status and creates an order.
   *
   * @param user     user that performed the action
   * @param customer the recipient of the order
   * @param car      car
   * @param kind     type
   * @param state    initial order state
   * @param comment  comment
   * @return the order created
   */
  public Order createOrder(int user, int customer, int car, OrderKind kind, OrderState state, String comment) {
    switch (kind) {
      case SERVICE -> {
        if (orders.findCarOrders(car).stream()
                  .noneMatch(o -> o.type() == OrderKind.PURCHASE &&
                                  o.state() == OrderState.DONE &&
                                  o.customer().id() == customer)) { //Check if the user has bought the car.
          throw customer == user ? new CarReservedException("Car " + car + " is not yours.")
                                 : new CarReservedException("Customer " + customer + " does not own car " + car + ".");
        }
      }
      case PURCHASE -> {
        if (orders.findCarOrders(car).stream()
                  .anyMatch(o -> o.type() == OrderKind.PURCHASE &&
                                 o.state() != OrderState.CANCELLED)) {
          throw new CarReservedException("Car " + car + " is not available for purchase.");
        }
      }
    }
    var order = orders.create(time.now(), kind, state, customer, car, comment);
    events.onOrderCreated(user, order);
    return order;
  }

  public Order findById(int orderId) {
    return orders.findById(orderId);
  }

  /**
   * Checks there are no service orders
   *
   * @param customerId customer id
   * @param carId      car id
   */
  private void validateNoServiceOrdersExist(int customerId, int carId) {
    if (orders.findCarOrders(carId)
              .stream()
              .filter(o -> o.customer().id() == customerId)
              .anyMatch(o -> o.type() == OrderKind.SERVICE)) {
      throw new CascadingException("Purchase order removal violates service order ownership constraints.");
    }
  }

  /**
   * Deletes the order verifying consistency
   *
   * @param deleterId id of the user that performed the action
   * @param orderId   order to be deleted
   */
  public void deleteOrder(int deleterId, int orderId) {
    var old = orders.findById(orderId);
    if (old.type() == OrderKind.PURCHASE) {
      validateNoServiceOrdersExist(old.customer().id(), old.car().id());
    }

    orders.delete(orderId);
    events.onOrderDeleted(deleterId, orderId);
  }

  public Order cancel(int userId, int orderId) {
    var order = orders.findById(orderId);
    switch (order.state()) {
      case CANCELLED -> throw new CommandException("This order has already been cancelled.");
      case DONE -> throw new CommandException("You cannot cancel finished orders.");
    }
    var newRow = orders.edit(orderId).state(OrderState.CANCELLED).apply();
    events.onOrderEdited(userId, newRow);
    return newRow;
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
    var order = orders.findById(orderId);

    if (newState != OrderState.DONE && order.state() == OrderState.DONE && order.type() == OrderKind.PURCHASE) {
      validateNoServiceOrdersExist(order.customer().id(), order.car().id());
    }

    var newRow = orders.edit(orderId).state(newState).comments(order.comments() + appendComment).apply();
    events.onOrderEdited(userId, newRow);
  }

  public List<Order> findMyOrders(int user, OrderSorting sorting) {
    return orders.findCustomerOrders(user, sorting);
  }

  public List<Order> findAllOrders(String username, String carBrand, String carModel, Collection<OrderState> states,
                                   OrderSorting sorting) {
    return orders.lookup(username, carBrand, carModel, EnumSet.copyOf(states), sorting);
  }
}
