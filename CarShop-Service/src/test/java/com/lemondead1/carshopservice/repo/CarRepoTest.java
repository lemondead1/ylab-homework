package com.lemondead1.carshopservice.repo;

import static org.assertj.core.api.Assertions.*;

import com.lemondead1.carshopservice.enums.CarSorting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CarRepoTest {
  private CarRepo cars;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
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
}
