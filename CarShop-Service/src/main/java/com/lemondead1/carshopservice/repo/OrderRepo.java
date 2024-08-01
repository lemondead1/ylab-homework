package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.service.LoggerService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class OrderRepo {
  private record PurchaseOrderStore(int id, Instant createdAt, OrderKind kind, OrderState state, int customerId,
                                    int carId, String comments) { }

  private final LoggerService logger;
  private final UserRepo users;
  private final CarRepo cars;

  private final Map<Integer, PurchaseOrderStore> map = new HashMap<>();
  private final Map<Integer, Set<PurchaseOrderStore>> customerOrders = new HashMap<>();
  private int lastId = 0;

  public OrderRepo(LoggerService logger, UserRepo users, CarRepo cars) {
    this.logger = logger;
    this.users = users;
    this.cars = cars;
  }

  public int create(Instant createdAt, OrderKind kind, OrderState state, int customerId, int carId, String comments) {
    users.findById(customerId);
    cars.findById(carId);
    lastId++;
    var newRow = new PurchaseOrderStore(lastId, createdAt, kind, state, customerId, carId, comments);
    map.put(lastId, newRow);
    customerOrders.computeIfAbsent(customerId, i -> new HashSet<>()).add(newRow);
    return lastId;
  }

  public void edit(int id, Instant newCreatedAt, OrderKind newKind, OrderState newState, int newCustomerId,
                   int newCarId, String newComments) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    var newRow = new PurchaseOrderStore(id, newCreatedAt, newKind, newState, newCustomerId, newCarId, newComments);
    var old = map.put(id, newRow);
    Objects.requireNonNull(old);
    var ordersSet = customerOrders.getOrDefault(old.customerId(), Collections.emptySet());
    if (!ordersSet.remove(old)) {
      logger.printf("Database is in an inconsistent state. %s is not in customerOrders.\n", old);
    }
    if (ordersSet.isEmpty()) {
      customerOrders.remove(old.customerId());
    }
    customerOrders.computeIfAbsent(newCustomerId, integer -> new HashSet<>()).add(newRow);
  }

  public Order delete(int id) {
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }
    var ordersSet = customerOrders.getOrDefault(old.customerId(), Collections.emptySet());
    if (!ordersSet.remove(old)) {
      logger.printf("Database is in an inconsistent state. %s is not in customerOrders.\n", old);
    }
    if (ordersSet.isEmpty()) {
      customerOrders.remove(old.customerId());
    }
    return new Order(old.id, old.createdAt, old.kind, old.state, users.findById(old.id), cars.findById(old.carId),
                     old.comments);
  }

  /**
   * Fetches order done by that customer.
   * Does not check whether the customer exists and in that case returns an empty set.
   *
   * @param customerId id of a customer
   * @return Stream of orders done by that customer
   */
  public Stream<Order> getCustomerOrders(int customerId) {
    return customerOrders.getOrDefault(customerId, Collections.emptySet()).stream()
                         .map(o -> new Order(o.id(), o.createdAt(), o.kind(), o.state(),
                                             users.findById(o.customerId()), cars.findById(o.carId()), o.comments()));
  }
}
