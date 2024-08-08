package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderRepoTest {
  CarRepo cars;
  UserRepo users;
  OrderRepo orders;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo();
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);

    users.create("alex", "88005553535", "test@example.com", "pwd", UserRole.CLIENT);
    cars.create("BMW", "X5", 2015, 3000000, "good");
  }

  @Test
  void firstCreatedOrderHasIdEqualToOne() {
    assertThat(orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "").id()).isEqualTo(1);
  }

  @Test
  void createdOrderMatchesSpec() {
    Instant now = Instant.now();
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    assertThat(orders.findById(1))
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW, users.findById(1), cars.findById(1), ""));
  }

  @Test
  void findCarOrdersContainsOrder() {
    Instant now = Instant.now();
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    assertThat(orders.findCarOrders(1))
        .containsExactly(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW,
                                   users.findById(1), cars.findById(1), ""));
  }

  @Test
  void findUserOrdersContainsOrder() {
    Instant now = Instant.now();
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    assertThat(orders.findCustomerOrders(1, OrderSorting.LATEST_FIRST))
        .containsExactly(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW,
                                   users.findById(1), cars.findById(1), ""));
  }

  @Test
  void editedOrderMatchesSpec() {
    Instant now = Instant.now();
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    orders.edit(1).state(OrderState.PERFORMING).comments("newComment").apply();
    assertThat(orders.findById(1))
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.PERFORMING,
                             users.findById(1), cars.findById(1), "newComment"));
  }

  @Test
  void editNonExistingOrderThrows() {
    var builder = orders.edit(1).state(OrderState.PERFORMING);
    assertThatThrownBy(builder::apply).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteTest() {
    Instant now = Instant.now();
    var created = orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    assertThat(orders.delete(1)).isEqualTo(created);
    assertThatThrownBy(() -> orders.findById(1)).isInstanceOf(RowNotFoundException.class);
    assertThat(orders.findCarOrders(1)).isEmpty();
    assertThat(orders.findCustomerOrders(1,OrderSorting.LATEST_FIRST)).isEmpty();
  }

  @Test
  void creatingOrderWithMissingCarThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 2, ""))
        .isInstanceOf(ForeignKeyException.class);
  }

  @Test
  void creatingOrderWithMissingUserThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 2, 1, ""))
        .isInstanceOf(ForeignKeyException.class);
  }
}
