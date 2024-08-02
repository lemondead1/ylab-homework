package com.lemondead1.carshopservice.repo;

import static org.assertj.core.api.Assertions.*;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.service.LoggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

public class CarRepoTest {
  private CarRepo cars;
  private UserRepo users;
  private OrderRepo orders;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo(new LoggerService());
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  void lookupCarsSortingTest(int sortingType) {
    cars.create("a", "model", 2000, 3000, "new");
    cars.create("K", "model", 2003, 3000, "new");
    cars.create("b", "model", 1999, 3000, "new");
    cars.create("c", "model", 2004, 3000, "new");
    cars.create("D", "model", 2000, 2999, "new");
    cars.create("E", "model", 2000, 3001, "new");
    cars.create("F", "model", 2003, 3000, "new");
    cars.create("G", "model", 2003, 3000, "new");
    cars.create("H", "model", 2003, 3000, "new");
    cars.create("I", "model", 2003, 3000, "new");
    cars.create("J", "model", 2003, 3000, "new");

    var sorting = CarSorting.values()[sortingType];
    assertThat(cars.lookupCars(null, null, null, null, null, sorting).findFirst().orElseThrow().id())
        .isEqualTo(sortingType + 1);
  }

  @Nested
  class LookupCarsFilterTests {
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

    //TODO add more cases
    @ParameterizedTest
    @CsvSource(value =
                   {
                       "'1', chev, cor, null, null, null",
                       "'4,5,11', null, an, null, null, null",
                       "'10,16', null, null, 1995, null, null",
                       "'2', null, null, null, 3130630, null",
                       "'3,6,12,13,15,16', null, null, null, null, air"
                   },
               nullValues = "null")
    void lookupTest(String expectedIds,
                    String brand,
                    String model,
                    Integer year,
                    Integer price,
                    String condition) {
      assertThat(cars.lookupCars(brand, model, year, price, condition, null).map(Car::id).toList())
          .containsExactly(Arrays.stream(expectedIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new));
    }
  }
}
