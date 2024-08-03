package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
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

  public Order createOrder(int user, int customer, int car, OrderKind kind, OrderState state, String comment) {
    return switch (kind) {
      case SERVICE -> {
        if (orders.findCarOrders(car).stream()
                  .noneMatch(o -> o.type() == OrderKind.PURCHASE &&
                                  o.state() == OrderState.DONE &&
                                  o.customer().id() == customer)) { //Check if the user has bought the car.
          throw customer == user ? new CarReservedException("Car " + car + " is not yours.")
                                 : new CarReservedException("Customer " + customer + " does not own car " + car + ".");
        }
        var order = orders.create(time.now(), OrderKind.SERVICE, state, customer, car, comment);
        events.onOrderCreated(user, order);
        yield order;
      }
      case PURCHASE -> {
        if (orders.findCarOrders(car).stream()
                  .anyMatch(o -> o.type() == OrderKind.PURCHASE &&
                                 o.state() != OrderState.CANCELLED)) {
          throw new CarReservedException("Car " + car + " is not available for purchase.");
        }
        var order = orders.create(time.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, comment);
        events.onOrderCreated(user, order);
        yield order;
      }
    };
  }

  public Order findById(int orderId) {
    return orders.findById(orderId);
  }

  public void deleteOrder(int deleterId, int orderId) {
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

  public void updateState(int userId, int orderId, OrderState newState, String appendComment) {
    var order = orders.findById(orderId);
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
