package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CarReservedException;
import com.lemondead1.carshopservice.repo.OrderRepo;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class OrderService {
  private final OrderRepo orders;
  private final EventService events;

  public OrderService(OrderRepo orders, EventService events) {
    this.orders = orders;
    this.events = events;
  }

  public Car createPurchaseOrder(int user, int carId, String comments) {
    if (orders.getCarOrders(carId).stream()
              .anyMatch(o -> o.type() == OrderKind.PURCHASE &&
                             o.state() != OrderState.CANCELLED)) {
      throw new CarReservedException("Car " + carId + " is not available for purchase.");
    }
    var order = orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, carId, comments);
    events.onOrderCreated(user, order.id(), order.createdAt(), order.type(), order.state(), order.id(), carId,
                          comments);
    return order.car();
  }

  public Car createServiceOrder(int user, int carId, String comments) {
    if (orders.getCarOrders(carId).stream()
              .anyMatch(o -> o.type() == OrderKind.PURCHASE &&
                             o.state() == OrderState.DONE &&
                             o.customer().id() == user)) { //Check if user has bought the car.
      throw new CarReservedException("Car " + carId + " is not yours.");
    }
    var order = orders.create(Instant.now(), OrderKind.SERVICE, OrderState.NEW, user, carId, comments);
    events.onOrderCreated(user, order.id(), order.createdAt(), order.type(), order.state(), order.id(), carId,
                          comments);
    return order.car();
  }

  public List<Order> findMyOrders(int user, OrderSorting sorting) {
    return orders.getCustomerOrders(user, sorting);
  }

  public List<Order> findAllOrders(String username, String carBrand, String carModel, OrderSorting sorting) {
    return orders.find(username, carBrand, carModel, sorting);
  }
}
