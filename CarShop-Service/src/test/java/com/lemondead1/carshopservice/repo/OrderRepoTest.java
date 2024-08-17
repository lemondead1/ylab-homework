package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.DateRangeConverter;
import com.lemondead1.carshopservice.HasIdEnumConverter;
import com.lemondead1.carshopservice.HasIdEnumSetConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderRepoTest {
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(),
                              postgres.getPassword(), "data", "infra", "db/changelog/test-changelog.yaml", true);
    dbManager.setupDatabase();
    cars = new CarRepo(dbManager);
    users = new UserRepo(dbManager);
    orders = new OrderRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @ParameterizedTest
  @CsvSource({
      "7,   2014-05-25T15:39:06Z, purchase, done,       9,  3, oPhTgnijbpDAvFifztcm",
      "65,  2014-06-27T18:53:10Z, service,  performing, 12, 9, kdRonPSrfcENjJHsVqMd",
      "11,  2014-05-27T00:16:58Z, service,  done,       3,  2, zdgCoETFHuopYGAhVpwU",
      "101, 2014-07-25T08:18:25Z, service,  performing, 3,  2, rrMKbrhgfLqLatUxYGch"
  })
  @DisplayName("findById returns the correct car.")
  void findByIdReturnsCorrectOrder(int id,
                                   Instant createdAt,
                                   @ConvertWith(HasIdEnumConverter.class) OrderKind kind,
                                   @ConvertWith(HasIdEnumConverter.class) OrderState state,
                                   int clientId,
                                   int carId,
                                   String comment) {
    assertThat(orders.findById(id))
        .isEqualTo(new Order(id, createdAt, kind, state, users.findById(clientId), cars.findById(carId), comment));
  }

  @ParameterizedTest
  @CsvSource({
      "purchase, done, 69, 81, BHPJowLzvtnBzTrURVYP",
      "service, new, 3, 6, BHPJowLzvtnBzTrURVYP",
  })
  @DisplayName("create adds an order matching arguments.")
  void createdOrderMatchesSpec(@ConvertWith(HasIdEnumConverter.class) OrderKind kind,
                               @ConvertWith(HasIdEnumConverter.class) OrderState state,
                               int clientId,
                               int carId,
                               String comment) {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var created = orders.create(now, kind, state, clientId, carId, comment);
    assertThat(created)
        .isEqualTo(orders.findById(created.id()))
        .isEqualTo(new Order(created.id(), now, kind, state, users.findById(clientId), cars.findById(carId), comment));
  }

  //TODO maybe add more testcases
  @ParameterizedTest
  @CsvSource({
      "'36, 189, 201, 203', 14",
      "'119', 50"
  })
  @DisplayName("findCarOrders returns orders that reference the given car.")
  void findCarOrdersTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds, int carId) {
    assertThat(orders.findCarOrders(carId))
        .allMatch(o -> orders.findById(o.id()).equals(o))
        .map(Order::id).containsExactlyInAnyOrder(expectedIds);
  }

  @ParameterizedTest
  @CsvSource({
      "'41, 52, 104, 134, 215, 233, 238, 248, 250, 251, 265, 277, 281, 290', 15",
      "'119', 7"
  })
  @DisplayName("findUserOrders returns orders that reference the given user.")
  void findUserOrdersContainsOrder(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds, int clientId) {
    assertThat(orders.findClientOrders(clientId, OrderSorting.CREATED_AT_DESC))
        .allMatch(o -> orders.findById(o.id()).equals(o))
        .map(Order::id).containsExactlyInAnyOrder(expectedIds);
  }

  @Test
  @DisplayName("edit changes the order's fields according to non-null arguments.")
  void editedOrderMatchesSpec() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var created = orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 88, 99, "");
    var edited = orders.edit(created.id(), null, null, OrderState.PERFORMING, null, null, "newComment");
    assertThat(edited).isEqualTo(orders.findById(created.id()))
                      .matches(o -> o.state() == OrderState.PERFORMING && "newComment".equals(o.comments()));
  }

  @Test
  @DisplayName("edit throws RowNotFoundException when a car with the given id does not exist.")
  void editNonExistingOrderThrows() {
    assertThatThrownBy(() -> orders.edit(1000, null, null, OrderState.PERFORMING, null, null, null))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("delete deletes.")
  void deleteTest() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var created = orders.create(now, OrderKind.PURCHASE, OrderState.NEW, 101, 98, "");
    // Note to self: returns of delete and create must not match.
    assertThat(orders.delete(created.id()))
        .isEqualTo(new Order(created.id(), now, OrderKind.PURCHASE,
                             OrderState.NEW, users.findById(101), cars.findById(98), ""));

    assertThatThrownBy(() -> orders.findById(created.id())).isInstanceOf(NotFoundException.class);
    assertThat(orders.findCarOrders(98)).isEmpty();
    assertThat(orders.findClientOrders(101, OrderSorting.CREATED_AT_DESC)).isEmpty();
  }

  @Test
  @DisplayName("create throws when there is no car with the given id.")
  void creatingOrderWithMissingCarThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 13461, ""))
        .isInstanceOf(DBException.class);
  }

  @Test
  @DisplayName("create throws when there is no user with the given id.")
  void creatingOrderWithMissingUserThrows() {
    assertThatThrownBy(() -> orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 3224, 1, ""))
        .isInstanceOf(DBException.class);
  }

  @Nested
  class LookupTest {
    @BeforeAll
    static void beforeAll() {
      dbManager.dropSchemas();
      dbManager.setupDatabase();
    }

    @ParameterizedTest
    @CsvSource({
        "'74, 75, 76, 77, 78, 79, 80, 81, 82, 83', 3.7.2014 - 10.7.2014, '', '',   '',   ALL,      ALL",
        "'57, 143, 173, 261',                      ALL,                  ab, '',   '',   ALL,      ALL",
        "'52, 206, 238, 250, 265, 277',            ALL,                  '', toyo, '',   ALL,      ALL",
        "'183, 218, 264',                          ALL,                  '', '',   pass, ALL,      ALL",
        "'75, 79, 82, 83',                         3.7.2014 - 10.7.2014, '', '',   '',   purchase, ALL",
        "'75, 76, 77, 81, 83',                     3.7.2014 - 10.7.2014, '', '',   '',   ALL,      performing",
    })
    @DisplayName("lookup returns rows matching arguments.")
    void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds,
                    @ConvertWith(DateRangeConverter.class) DateRange dateRange,
                    String customerName,
                    String brand,
                    String model,
                    @ConvertWith(HasIdEnumSetConverter.class) Set<OrderKind> kinds,
                    @ConvertWith(HasIdEnumSetConverter.class) Set<OrderState> states) {
      assertThat(orders.lookup(dateRange, customerName, brand, model, kinds, states, OrderSorting.CREATED_AT_DESC))
          .map(Order::id).containsExactlyInAnyOrder(expectedIds);
    }

    @Test
    void sortingTestDateDesc() {
      assertThat(orders.lookup(DateRange.ALL, "", "", "", OrderKind.ALL_SET, OrderState.ALL_SET, OrderSorting.CREATED_AT_DESC))
          .isSortedAccordingTo(Comparator.comparing(Order::createdAt).reversed()).hasSize(290);
    }

    @Test
    void sortingTestDateAsc() {
      assertThat(orders.lookup(DateRange.ALL, "", "", "", OrderKind.ALL_SET, OrderState.ALL_SET, OrderSorting.CREATED_AT_ASC))
          .isSortedAccordingTo(Comparator.comparing(Order::createdAt)).hasSize(290);
    }

    @Test
    void sortingTestCarNameDesc() {
      assertThat(orders.lookup(DateRange.ALL, "", "", "", OrderKind.ALL_SET, OrderState.ALL_SET, OrderSorting.CAR_NAME_DESC))
          .isSortedAccordingTo(Comparator.comparing((Order o) -> o.car().getBrandModel(), String::compareToIgnoreCase).reversed())
          .hasSize(290);
    }

    @Test
    void sortingTestCarNameAsc() {
      assertThat(orders.lookup(DateRange.ALL, "", "", "", OrderKind.ALL_SET, OrderState.ALL_SET, OrderSorting.CAR_NAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(o -> o.car().getBrandModel(), String::compareToIgnoreCase))
          .hasSize(290);
    }
  }
}
