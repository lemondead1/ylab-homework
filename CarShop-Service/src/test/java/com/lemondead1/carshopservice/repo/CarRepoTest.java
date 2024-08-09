package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.IntRangeConverter;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarRepoTest {
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
    dbManager.init();
  }

  @AfterEach
  void afterEach() {
    dbManager.dropAll();
  }

  @Test
  void firstCreatedCarHasIdEqualToOne() {
    assertThat(cars.create("BMW", "X5", 2015, 3000000, "good").id()).isEqualTo(1);
    assertThat(cars.findById(1).id()).isEqualTo(1);
  }

  @Test
  void createdCarMatchesSpec() {
    assertThat(cars.create("BMW", "X5", 2015, 3000000, "good").id()).isEqualTo(1);
    assertThat(cars.create("Lamborghini", "Diablo", 2017, 6000000, "fair").id()).isEqualTo(2);
    assertThat(cars.findById(2)).isEqualTo(new Car(2, "Lamborghini", "Diablo", 2017, 6000000, "fair", true));
  }

  @Test
  void editedCarMatchesSpec() {
    cars.create("BMW", "X5", 2015, 3000000, "good");
    cars.edit(1, null, null, null, 4000000, "mint");
    assertThat(cars.findById(1)).isEqualTo(new Car(1, "BMW", "X5", 2015, 4000000, "mint", true));
  }

  @Test
  void editNonExistingCarThrows() {
    assertThatThrownBy(() -> cars.edit(1, null, null, null, 3000000, null)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void deleteTest() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    assertThat(cars.delete(1)).isEqualTo(created);
    assertThatThrownBy(() -> cars.findById(1)).isInstanceOf(RowNotFoundException.class);
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
    @BeforeEach
    void beforeEach() {
      var csv = """
          Chevrolet,Corvette,1955,2824895,excellent
          Lamborghini,Diablo,1993,3130630,good
          Ford,F250,2010,4363963,fair
          Ford,Mustang,2013,3095716,excellent
          Chevrolet,Suburban 1500,1994,433660,good
          Dodge,Shadow,1993,1981507,fair
          Infiniti,I,1996,300127,good
          Nissan,Armada,2008,2746367,excellent
          Mercury,Capri,1985,598761,poor
          Toyota,T100,1995,3690260,excellent
          Pontiac,Grand Prix,1997,4657296,good
          Mitsubishi,Truck,1989,3127633,fair
          Nissan,Maxima,1999,3729862,fair
          Chevrolet,Blazer,2003,924572,excellent
          Ford,F350,2001,1434159,fair
          Hyundai,Sonata,1995,4179685,fair""";

      Arrays.stream(csv.split("\n"))
            .map(r -> r.split(","))
            .forEachOrdered(r -> cars.create(r[0], r[1], Integer.parseInt(r[2]), Integer.parseInt(r[3]), r[4]));
    }

    @Test
    void sortingTestNameAsc() {
      assertThat(cars.lookup("", "", IntRange.ALL, IntRange.ALL, "", Set.of(true, false), CarSorting.NAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase))
          .map(Car::id).contains(IntStream.range(1, 16).boxed().toArray(Integer[]::new));
    }

    //TODO add more cases
    @ParameterizedTest
    @CsvSource(value = {
        "'1',               chev, cor, ALL,  ALL,               ''",
        "'4,5,11',          '',   an,  ALL,  ALL,               ''",
        "'10,16',           '',   '',  1995, ALL,               ''",
        "'2,4,10,12,13',    '',   '',  ALL,  3000000 - 4000000, ''",
        "'3,6,12,13,15,16', '',   '',  ALL,  ALL,               air"
    })
    void lookupTest(String expectedIds,
                    String brand,
                    String model,
                    @ConvertWith(IntRangeConverter.class) IntRange year,
                    @ConvertWith(IntRangeConverter.class) IntRange price,
                    String condition) {
      assertThat(cars.lookup(brand, model, year, price, condition, Set.of(true, false), CarSorting.NAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase))
          .map(Car::id)
          .contains(Arrays.stream(expectedIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new));
    }
  }
}
