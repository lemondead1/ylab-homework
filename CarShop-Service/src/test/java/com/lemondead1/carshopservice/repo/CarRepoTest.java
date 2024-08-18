package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.RangeConverter;
import com.lemondead1.carshopservice.TestDBConnector;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarRepoTest {
  private static final CarRepo cars = new CarRepo(TestDBConnector.DB_MANAGER);

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @ParameterizedTest(name = "findById({0}) returns Car({0}, {1}, {2}, {3}, {4}, {5}, {6}).")
  @CsvSource({
      "52, Dodge,      Viper,     1993, 9322589, fair, false",
      "6,  Land Rover, Discovery, 2005, 6085538, poor, false",
      "96, Buick,      Century,   1997, 1497126, fair, true"
  })
  @DisplayName("findById returns a car with required id.")
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
  @DisplayName("create creates a new car that matches arguments.")
  void createdCarMatchesSpec() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    assertThat(created)
        .isEqualTo(cars.findById(created.id()))
        .isEqualTo(new Car(created.id(), "BMW", "X5", 2015, 3000000, "good", true));
  }

  @Test
  @DisplayName("edit changes the car's fields according to non-null arguments.")
  void editedCarMatchesSpec() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    var edited = cars.edit(created.id(), null, null, null, 4000000, "mint");
    assertThat(edited)
        .isEqualTo(cars.findById(created.id()))
        .isEqualTo(new Car(created.id(), "BMW", "X5", 2015, 4000000, "mint", true));
  }

  @Test
  @DisplayName("edit throws RowNotFoundException a car when the given id is not found.")
  void editNonExistingCarThrows() {
    assertThatThrownBy(() -> cars.edit(999, null, null, null, 3000000, null)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("delete deletes the car.")
  void deleteTest() {
    var created = cars.create("BMW", "X5", 2015, 3000000, "good");
    assertThat(cars.delete(created.id())).isEqualTo(created);
    assertThatThrownBy(() -> cars.findById(created.id())).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("delete throws when the car does not exist.")
  void deleteNotFoundTest() {
    assertThatThrownBy(() -> cars.delete(1000)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("delete throws if there exist orders referencing the car.")
  void deletingCarWithExistingOrdersThrows() {
    assertThatThrownBy(() -> cars.delete(1)).isInstanceOf(DBException.class);
  }

  private static final Set<Boolean> allBool = Set.of(true, false);

  @Test
  void sortingTestNameAsc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.NAME_ASC))
        .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase))
        .hasSize(100);
  }

  @Test
  void sortingTestNameDesc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.NAME_DESC))
        .isSortedAccordingTo(Comparator.comparing(Car::getBrandModel, String::compareToIgnoreCase).reversed())
        .hasSize(100);
  }

  @Test
  void sortingTestProductionYearAsc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.PRODUCTION_YEAR_ASC))
        .isSortedAccordingTo(Comparator.comparing(Car::productionYear))
        .hasSize(100);
  }

  @Test
  void sortingTestProductionYearDesc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.PRODUCTION_YEAR_DESC))
        .isSortedAccordingTo(Comparator.comparing(Car::productionYear).reversed())
        .hasSize(100);
  }

  @Test
  void sortingTestPriceAsc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.PRICE_ASC))
        .isSortedAccordingTo(Comparator.comparing(Car::price))
        .hasSize(100);
  }

  @Test
  void sortingTestPriceDesc() {
    assertThat(cars.lookup("", "", Range.all(), Range.all(), "", allBool, CarSorting.PRICE_DESC))
        .isSortedAccordingTo(Comparator.comparing(Car::price).reversed())
        .hasSize(100);
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", value = {
      "'27, 28, 50, 66, 80, 88', chev, '',  ALL,         ALL,               '',   null",
      "'28, 80',                 '',   cor, ALL,         ALL,               '',   null",
      "'19, 22, 31, 39, 62, 93', '',   '',  2000 - 2001, ALL,               '',   null",
      "'19, 23, 39, 71, 73',     '',   '',  ALL,         3000000 - 3500000, '',   null",
      "'11, 40, 80',             '',   '',  ALL,         0 - 2000000,       good, null",
      "'90, 96, 97, 100',        '',   '',  ALL,         0 - 2000000,       '',   true",
  })
  @DisplayName("lookup returns entries matching arguments.")
  void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds,
                  String brand,
                  String model,
                  @ConvertWith(RangeConverter.class) Range<Integer> year,
                  @ConvertWith(RangeConverter.class) Range<Integer> price,
                  String condition,
                  @Nullable Boolean available) {
    var availableSet = available == null ? allBool : Set.of(available);
    assertThat(cars.lookup(brand, model, year, price, condition, availableSet, CarSorting.NAME_ASC))
        .map(Car::id).containsExactlyInAnyOrder(expectedIds);
  }
}
