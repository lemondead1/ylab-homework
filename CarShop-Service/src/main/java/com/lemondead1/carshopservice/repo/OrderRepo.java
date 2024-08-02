package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.service.LoggerService;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class OrderRepo {
  private record OrderStore(int id, Instant createdAt, OrderKind kind, OrderState state, int customerId, int carId,
                            String comments) { }

  private final LoggerService logger;

  private UserRepo users;

  public void setUsers(UserRepo users) {
    this.users = users;
  }

  private CarRepo cars;

  public void setCars(CarRepo cars) {
    this.cars = cars;
  }

  private final Map<Integer, OrderStore> map = new HashMap<>();
  private final Map<Integer, Set<OrderStore>> customerOrders = new HashMap<>();
  private final Map<Integer, Set<OrderStore>> carOrders = new HashMap<>();
  private int lastId = 0;

  public OrderRepo(LoggerService logger) {
    this.logger = logger;
  }

  public Order create(Instant createdAt, OrderKind kind, OrderState state, int customerId, int carId, String comments) {
    var user = users.findById(customerId);
    var car = cars.findById(carId);
    lastId++;
    var newRow = new OrderStore(lastId, createdAt, kind, state, customerId, carId, comments);
    map.put(lastId, newRow);
    customerOrders.computeIfAbsent(customerId, i -> new HashSet<>()).add(newRow);
    carOrders.computeIfAbsent(carId, i -> new HashSet<>()).add(newRow);
    return new Order(lastId, createdAt, kind, state, user, car, comments);
  }

  public void edit(int id, Instant newCreatedAt, OrderKind newKind, OrderState newState, int newCustomerId,
                   int newCarId, String newComments) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    var newRow = new OrderStore(id, newCreatedAt, newKind, newState, newCustomerId, newCarId, newComments);
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

    var carOrdersSet = carOrders.getOrDefault(old.carId(), Collections.emptySet());
    if (!carOrdersSet.remove(old)) {
      logger.printf("Database is in an inconsistent state. %s is not in carOrders.\n", old);
    }
    if (carOrdersSet.isEmpty()) {
      carOrders.remove(old.carId());
    }
    carOrders.computeIfAbsent(newCarId, i -> new HashSet<>()).add(newRow);
  }

  public Order delete(int id) {
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }

    var clientOrdersSet = customerOrders.getOrDefault(old.customerId(), Collections.emptySet());
    if (!clientOrdersSet.remove(old)) {
      logger.printf("Database is in an inconsistent state. %s is not in customerOrders.\n", old);
    }
    if (clientOrdersSet.isEmpty()) {
      customerOrders.remove(old.customerId());
    }

    var carOrdersSet = carOrders.getOrDefault(old.carId(), Collections.emptySet());
    if (!carOrdersSet.remove(old)) {
      logger.printf("Database is in an inconsistent state. %s is not in carOrders.\n", old);
    }
    if (carOrdersSet.isEmpty()) {
      carOrders.remove(old.carId());
    }

    return new Order(old.id, old.createdAt, old.kind, old.state,
                     users.findById(old.id), cars.findById(old.carId), old.comments);
  }

  public boolean existCustomerOrders(int customerId) {
    return customerOrders.containsKey(customerId);
  }

  public boolean existCarOrders(int carId) {
    return carOrders.containsKey(carId);
  }

  /**
   * Fetches order done by that customer.
   * Does not check whether the customer exists and in that case returns an empty list.
   *
   * @param customerId id of a customer
   * @return List of orders done by that customer
   */
  public List<Order> getCustomerOrders(int customerId, OrderSorting sorting) {
    List<Order> list = new ArrayList<>();
    for (var o : customerOrders.getOrDefault(customerId, Collections.emptySet())) {
      Order order = new Order(o.id(), o.createdAt(), o.kind(), o.state(),
                              users.findById(o.customerId()), cars.findById(o.carId()), o.comments());
      list.add(order);
    }
    Comparator<Order> sorter = switch (sorting) {
      case LATEST_FIRST -> Comparator.comparing(Order::createdAt).reversed();
      case OLDEST_FIRST -> Comparator.comparing(Order::createdAt);
      case CAR_NAME_DESC ->
          Comparator.comparing((Order o) -> o.car().brand().toLowerCase() + " " + o.car().model().toLowerCase())
                    .reversed();
      case CAR_NAME_ASC ->
          Comparator.comparing((Order o) -> o.car().brand().toLowerCase() + " " + o.car().model().toLowerCase());
    };
    list.sort(sorter);
    return list;
  }

  public Stream<Order> listAll() {
    return map.values().stream().map(
        o -> new Order(o.id(), o.createdAt(), o.kind(), o.state(), users.findById(o.customerId()),
                       cars.findById(o.carId()), o.comments()));
  }

  public List<Order> find(@Nullable String customerName, @Nullable String carBrand, @Nullable String carModel,
                          OrderSorting sorting) {
    var stream = listAll();
    if (customerName != null) {
      var customerNameLower = customerName.toLowerCase();
      stream = stream.filter(o -> o.customer().username().toLowerCase().contains(customerNameLower));
    }
    if (carBrand != null) {
      var carBrandLower = carBrand.toLowerCase();
      stream = stream.filter(o -> o.car().brand().toLowerCase().contains(carBrandLower));
    }
    if (carModel != null) {
      var carModelLower = carModel.toLowerCase();
      stream = stream.filter(o -> o.car().model().toLowerCase().contains(carModelLower));
    }
    Comparator<Order> sorter = switch (sorting) {
      case LATEST_FIRST -> Comparator.comparing(Order::createdAt).reversed();
      case OLDEST_FIRST -> Comparator.comparing(Order::createdAt);
      case CAR_NAME_DESC ->
          Comparator.comparing((Order o) -> o.car().brand().toLowerCase() + " " + o.car().model().toLowerCase())
                    .reversed();
      case CAR_NAME_ASC ->
          Comparator.comparing((Order o) -> o.car().brand().toLowerCase() + " " + o.car().model().toLowerCase());
    };
    return stream.sorted(sorter).toList();
  }

  public List<Order> getCarOrders(int carId) {
    List<Order> list = new ArrayList<>();
    for (var o : carOrders.getOrDefault(carId, Collections.emptySet())) {
      Order order = new Order(o.id(), o.createdAt(), o.kind(), o.state(),
                              users.findById(o.customerId()), cars.findById(o.carId()), o.comments());
      list.add(order);
    }
    return list;
  }
}
