package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.service.LoggerService;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public class OrderRepo {
  private record OrderStore(int id, Instant createdAt, OrderKind kind, OrderState state, int customerId, int carId,
                            String comments) { }

  private final LoggerService logger;

  @Setter
  private UserRepo users;

  @Setter
  private CarRepo cars;

  private final Map<Integer, OrderStore> map = new HashMap<>();
  private final Map<Integer, Set<OrderStore>> customerOrders = new HashMap<>();
  private final Map<Integer, Set<OrderStore>> carOrders = new HashMap<>();
  private int lastId = 0;

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

  /**
   * @return order after edit
   */
  @Builder(builderMethodName = "", buildMethodName = "apply", builderClassName = "EditBuilder")
  private Order applyEdit(int id,
                          @Nullable Instant createdAt,
                          @Nullable OrderKind kind,
                          @Nullable OrderState state,
                          @Nullable Integer customerId,
                          @Nullable Integer carId,
                          @Nullable String comments) {
    var newCustomer = customerId == null ? null : users.findById(customerId);
    var newCar = carId == null ? null : cars.findById(carId);

    var old = delete(id);
    newCustomer = customerId == null ? old.customer() : newCustomer;
    newCar = carId == null ? old.car() : newCar;
    createdAt = createdAt == null ? old.createdAt() : createdAt;
    kind = kind == null ? old.type() : kind;
    state = state == null ? old.state() : state;
    comments = comments == null ? old.comments() : comments;

    var newRow = new OrderStore(id, createdAt, kind, state, newCustomer.id(), newCar.id(), comments);
    map.put(id, newRow);
    customerOrders.computeIfAbsent(newCustomer.id(), i -> new HashSet<>()).add(newRow);
    carOrders.computeIfAbsent(newCar.id(), i -> new HashSet<>()).add(newRow);
    return new Order(id, createdAt, kind, state, newCustomer, newCar, comments);
  }

  public EditBuilder edit(int id) {
    return new EditBuilder().id(id);
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

  private Order hydrateOrder(OrderStore order) {
    return new Order(order.id, order.createdAt, order.kind, order.state,
                     users.findById(order.id), cars.findById(order.carId), order.comments);
  }

  public Order lookup(int id) {
    var order = map.get(id);
    if (order == null) {
      throw new RowNotFoundException("Order " + id + " not found.");
    }
    return hydrateOrder(order);
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
    return customerOrders.getOrDefault(customerId, Set.of())
                         .stream()
                         .map(this::hydrateOrder)
                         .sorted(sorting.getSorter())
                         .toList();
  }

  public List<Order> lookup(String customerName,
                            String carBrand,
                            String carModel,
                            Set<OrderState> states,
                            OrderSorting sorting) {
    return map.values()
              .stream()
              .map(this::hydrateOrder)
              .filter(o -> StringUtil.containsIgnoreCase(o.customer().username(), customerName))
              .filter(o -> StringUtil.containsIgnoreCase(o.car().brand(), carBrand))
              .filter(o -> StringUtil.containsIgnoreCase(o.car().model(), carModel))
              .filter(o -> states.contains(o.state()))
              .sorted(sorting.getSorter())
              .toList();
  }

  public List<Order> getCarOrders(int carId) {
    return carOrders.getOrDefault(carId,Set.of()).stream().map(this::hydrateOrder).toList();
  }
}
