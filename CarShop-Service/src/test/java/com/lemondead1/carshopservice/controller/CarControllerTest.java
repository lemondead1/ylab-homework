package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.Availability;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.TableFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarControllerTest {
  @Mock
  CarService cars;

  @Mock
  UserService users;

  MockConsoleIO cli;

  CarController car;

  @BeforeEach
  void setup() {
    car = new CarController(cars);

    cli = new MockConsoleIO();
  }

  @Test
  void byIdThrowsWithoutArguments() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> car.byId(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(cars, users);
  }

  @Test
  void byIdSuccess() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    when(cars.findById(3)).thenReturn(dummyCar);

    assertThat(car.byId(dummyUser, cli, "3")).isEqualTo("Found " + dummyCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).findById(3);
  }

  @Test
  void carDeleteFailsWhenNoParameterIsPresent() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);

    assertThatThrownBy(() -> car.deleteCar(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(cars, users);
  }

  @Test
  void carDeleteCancelled() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(car.deleteCar(dummyUser, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
  }

  @Test
  void carDeleteCascadeCancelled() {
    var dummyUser = new User(5, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);
    var dummyCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(dummyCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);

    assertThat(car.deleteCar(dummyUser, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
    verify(cars, times(0)).deleteCar(5, 3, true);
  }

  @Test
  void carDeleteSuccess() {
    var dummyUser = new User(5, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doNothing().when(cars).deleteCar(5, 3, false);

    assertThat(car.deleteCar(dummyUser, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
  }

  @Test
  void carDeleteCascadingSuccess() {
    var dummyUser = new User(5, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);
    doNothing().when(cars).deleteCar(5, 3, true);

    assertThat(car.deleteCar(dummyUser, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
    verify(cars).deleteCar(5, 3, true);
  }

  @Test
  void carEditFailsWithNoArgument() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);

    assertThatThrownBy(() -> car.editCar(dummyUser, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(cars);
  }

  @Test
  void carEditSuccess() {
    var dummyUser = new User(5, "username", "12346789", "mail@example.com", "pass", UserRole.ADMIN, 0);
    var oldCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);
    var newCar = new Car(3, "Brand", "Model", 2001, 2000000, "ok", true);

    cli.out("Brand (Brand) > ").in("")
       .out("Model (Model) > ").in("")
       .out("Production year (2001) > ").in("2001")
       .out("Price (1000000) > ").in("2000000")
       .out("Condition (poor) > ").in("ok");

    when(cars.findById(3)).thenReturn(oldCar);
    when(cars.editCar(5, 3, null, null, 2001, 2000000, "ok")).thenReturn(newCar);

    assertThat(car.editCar(dummyUser, cli, "3")).isEqualTo("Saved changes to " + newCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).findById(3);
    verify(cars).editCar(5, 3, null, null, 2001, 2000000, "ok");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void createCarSuccess() {
    var dummyUser = new User(6, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Brand > ").in("Brand")
       .out("Model > ").in("Model")
       .out("Production year > ").in("2001")
       .out("Price > ").in("1000000")
       .out("Condition > ").in("poor");

    when(cars.createCar(6, "Brand", "Model", 2001, 1000000, "poor")).thenReturn(mockCar);

    assertThat(car.createCar(dummyUser, cli)).isEqualTo("Created " + mockCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).createCar(6, "Brand", "Model", 2001, 1000000, "poor");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void listCarsTest() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.CLIENT, 0);
    Car[] dummyCars = {
        new Car(3, "Brand", "Model", 2001, 1000000, "poor", false),
        new Car(64, "Toyota", "Corolla", 2009, 2000000, "ok", true)
    };

    cli.out("Brand > ").in("")
       .out("Model > ").in("")
       .out("Prod. year > ").in("2000 - 2010")
       .out("Price > ").in("100000 - 10000000")
       .out("Condition > ").in("o")
       .out("Availability for purchase > ").in("available,unavailable")
       .out("Sorting > ").in("");

    when(cars.lookupCars("", "", new IntRange(2000, 2010), new IntRange(100000, 10000000), "o",
                         List.of(Availability.AVAILABLE, Availability.UNAVAILABLE), CarSorting.NAME_ASC))
        .thenReturn(List.of(dummyCars));

    var tab = new TableFormatter("ID", "Brand", "Model", "Prod. year", "Price", "Condition", "Available for purchase");
    tab.addRow(3, "Brand", "Model", 2001, 1000000, "poor", "No");
    tab.addRow(64, "Toyota", "Corolla", 2009, 2000000, "ok", "Yes");

    assertThat(car.listCars(dummyUser, cli)).isEqualTo(tab.format(true));

    cli.assertMatchesHistory();
  }
}
