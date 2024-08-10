package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderRepoTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @BeforeEach
  void beforeEach() {
    dbManager.setupDatabase();
    users.create("test_user_1", "88005553535", "test@example.com", "pass", UserRole.CLIENT);
    cars.create("Tesla", "Model 3", 2020, 4000000, "good");
  }

  @AfterEach
  void afterEach() {
    dbManager.dropSchemas();
  }

  @Test
  void createdOrderMatchesSpec() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS); //Postgres does not support finer units
    var created = orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 24, 61, "");
    assertThat(created)
        .isEqualTo(orders.findById(created.id()))
        .isEqualTo(new Order(created.id(), now, OrderKind.PURCHASE, OrderState.NEW, users.findById(24), cars.findById(61), ""));
  }

  @Test
  void findCarOrdersContainsOrder() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    assertThat(orders.findClientOrders(1, OrderSorting.LATEST_FIRST))
        .singleElement()
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW, users.findById(1), cars.findById(1), ""));
  }

  @Test
  void findUserOrdersContainsOrder() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");

    assertThat(orders.findClientOrders(1, OrderSorting.LATEST_FIRST))
        .singleElement()
        .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW, users.findById(1), cars.findById(1), ""));
  }

  @Test
  void editedOrderMatchesSpec() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    var edited = orders.edit(1, null, null, OrderState.PERFORMING, null, null, "newComment");
    assertThat(edited).isEqualTo(orders.findById(1))
                      .isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.PERFORMING,
                                           users.findById(1), cars.findById(1), "newComment"));
  }

  @Test
  void editNonExistingOrderThrows() {
    assertThatThrownBy(() -> orders.edit(1, null, null, OrderState.PERFORMING, null, null, null))
        .isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteTest() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 1, 1, "");
    var deleted = orders.delete(1);
    assertThatThrownBy(() -> orders.findById(1)).isInstanceOf(RowNotFoundException.class);
    assertThat(deleted).isEqualTo(new Order(1, now, OrderKind.PURCHASE, OrderState.NEW,
                                            users.findById(1), cars.findById(1), ""));
    assertThat(orders.findCarOrders(1)).isEmpty();
    assertThat(orders.findClientOrders(1, OrderSorting.LATEST_FIRST)).isEmpty();
  }

  @Test
  void creatingOrderWithMissingCarThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 2, ""))
        .isInstanceOf(DBException.class);
  }

  @Test
  void creatingOrderWithMissingUserThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 2, 1, ""))
        .isInstanceOf(DBException.class);
  }
}
