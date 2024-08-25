package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.annotations.Timed;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.ConflictException;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.util.Range;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Service
@Timed
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final OrderRepo orders;
  private final CarRepo cars;
  private final UserRepo users;

  @Transactional
  @Audited(EventType.ORDER_CREATED)
  @Override
  public Order createOrder(@Audited.Param("client_id") int clientId,
                           @Audited.Param("car_id") int carId,
                           @Audited.Param("kind") OrderKind kind,
                           @Audited.Param("state") OrderState state,
                           @Audited.Param("comment") String comments) {
    Car car = cars.findById(carId);
    users.findById(clientId); //Check if the user exists.

    if (kind == OrderKind.PURCHASE && !car.availableForPurchase()) {
      throw new ConflictException("Car #" + carId + " is not available for purchase.");
    }

    //Check if the client has bought the car.
    if (kind == OrderKind.SERVICE && cars.findCarOwner(carId).map(User::id).map(id -> id != clientId).orElse(true)) {
      throw new ConflictException("Client #" + clientId + " is not the owner of #" + carId + ".");
    }

    return orders.create(Instant.now(), kind, state, clientId, carId, comments);
  }

  @Transactional
  @Override
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

  @Transactional
  @Audited(EventType.ORDER_DELETED)
  @Override
  public void deleteOrder(@Audited.Param("order_id") int orderId) {
    Order old = orders.findById(orderId);
    if (old.type() == OrderKind.PURCHASE) {
      checkNoServiceOrdersExist(old.client().id(), old.car().id());
    }

    orders.delete(orderId);
  }

  @Transactional
  @Audited(EventType.ORDER_MODIFIED)
  @Override
  public Order updateState(@Audited.Param("order_id") int orderId,
                           @Audited.Param("new_state") @Nullable OrderState newState,
                           @Audited.Param("appended_comment") @Nullable String appendComment) {
    Order oldOrder = orders.findById(orderId);

    if (oldOrder.state() == OrderState.DONE && oldOrder.type() == OrderKind.PURCHASE &&
        newState != null && newState != OrderState.DONE) {
      checkNoServiceOrdersExist(oldOrder.client().id(), oldOrder.car().id());
    }

    return orders.edit(orderId, null, null,
                       newState, null, null,
                       oldOrder.comments() + (StringUtils.isEmpty(appendComment) ? "" : "\n" + appendComment));
  }

  @Transactional
  @Override
  public List<Order> findClientOrders(int userId, OrderSorting sorting) {
    return orders.findClientOrders(userId, sorting);
  }

  @Transactional
  @Override
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
