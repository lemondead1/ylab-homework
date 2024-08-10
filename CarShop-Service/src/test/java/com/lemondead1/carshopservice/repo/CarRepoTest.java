package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.BooleanSetConverter;
import com.lemondead1.carshopservice.IntRangeConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.IntRange;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarRepoTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static CarRepo cars;
  static UserRepo users;
  static OrderRepo orders;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "data", "infra");
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
      "52, Dodge,      Viper,     1993, 9322589, fair, false",
      "6,  Land Rover, Discovery, 2005, 6085538, poor, false",
      "96, Buick,      Century,   1997, 1497126, fair, true"
  })
  void findByIdReturnsCorrectCar(int id,
                                 String brand,
                                 String model,
                                 int productionYear,
                                 int price,
                                 String condition,
                                 boolean available) {
    assertThat(cars.findById(id)).isEqualTo(new Car(id, brand, model, productionYear, price, condition, available));
  }


  @Test
  void createdCarMatchesSpec() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    assertThat(created)
        .isEqualTo(cars.findById(created.id()))
        .isEqualTo(new Car(created.id(), "BMW", "X5", 2015, 3000000, "good", true));
  }

  @Test
  void editedCarMatchesSpec() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    var edited = cars.edit(created.id(), null, null, null, 4000000, "mint");
    assertThat(edited)
        .isEqualTo(cars.findById(created.id()))
        .isEqualTo(new Car(created.id(), "BMW", "X5", 2015, 4000000, "mint", true));
  }

  @Test
  void editNonExistingCarThrows() {
    assertThatThrownBy(() -> cars.edit(999, null, null, null, 3000000, null)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteTest() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    assertThat(cars.delete(created.id())).isEqualTo(created);
    assertThatThrownBy(() -> cars.findById(created.id())).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deletingCarWithExistingOrdersThrows() {
    cars.create("BMW", "X5", 2015, 3000000, "good");
    users.create("alex", "88005553535", "test@example.com", "pwd", UserRole.CLIENT);
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "ASAP");
    assertThatThrownBy(() -> cars.delete(1)).isInstanceOf(DBException.class);
  }

  @Nested
  class LookupCarsTests {
    private static final Set<Boolean> allBool = Set.of(true, false);

    @BeforeAll
    static void beforeAll() {
      dbManager.dropSchemas();
      dbManager.setupDatabase();
    }

    @Test
    void sortingTestNameAsc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.NAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase))
          .hasSize(100);
    }

    @Test
    void sortingTestNameDesc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.NAME_DESC))
          .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase).reversed())
          .hasSize(100);
    }

    @Test
    void sortingTestProductionYearAsc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.PRODUCTION_YEAR_ASC))
          .isSortedAccordingTo(Comparator.comparing(Car::productionYear))
          .hasSize(100);
    }

    @Test
    void sortingTestProductionYearDesc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.PRODUCTION_YEAR_DESC))
          .isSortedAccordingTo(Comparator.comparing(Car::productionYear).reversed())
          .hasSize(100);
    }

    @Test
    void sortingTestPriceAsc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.PRICE_ASC))
          .isSortedAccordingTo(Comparator.comparing(Car::price))
          .hasSize(100);
    }

    @Test
    void sortingTestPriceDesc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", allBool, CarSorting.PRICE_DESC))
          .isSortedAccordingTo(Comparator.comparing(Car::price).reversed())
          .hasSize(100);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'27, 28, 50, 66, 80, 88', chev, '',  ALL,         ALL,               '',   ALL",
        "'28, 80',                 '',   cor, ALL,         ALL,               '',   ALL",
        "'19, 22, 31, 39, 62, 93', '',   '',  2000 - 2001, ALL,               '',   ALL",
        "'19, 23, 39, 71, 73',     '',   '',  ALL,         3000000 - 3500000, '',   ALL",
        "'11, 40, 80',             '',   '',  ALL,         0 - 2000000,       good, ALL",
        "'90, 96, 97, 100',        '',   '',  ALL,         0 - 2000000,       '',   true",
    })
    void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds,
                    String brand,
                    String model,
                    @ConvertWith(IntRangeConverter.class) IntRange year,
                    @ConvertWith(IntRangeConverter.class) IntRange price,
                    String condition,
                    @ConvertWith(BooleanSetConverter.class) Set<Boolean> available) {
      assertThat(cars.lookup(brand, model, year, price, condition, available, CarSorting.NAME_ASC))
          .map(Car::id).containsExactlyInAnyOrder(expectedIds);
    }
  }
}
