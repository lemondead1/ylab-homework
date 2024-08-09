package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.enums.Availability;
import com.lemondead1.carshopservice.enums.CarSorting;
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
  SessionService session;

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
    assertThatThrownBy(() -> car.byId(session, cli)).isInstanceOf(WrongUsageException.class);

    verifyNoInteractions(cars, users);
  }

  @Test
  void byIdSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(car.byId(session, cli, "3")).isEqualTo("Found " + mockCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).findById(3);
  }

  @Test
  void carDeleteFailsWhenNoParameterIsPresent() {
    assertThatThrownBy(() -> car.deleteCar(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(cars, users);
  }

  @Test
  void carDeleteCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
  }

  @Test
  void carDeleteCascadeCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
    verify(cars, times(0)).deleteCar(5, 3, true);
  }

  @Test
  void carDeleteSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doNothing().when(cars).deleteCar(5, 3, false);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
  }

  @Test
  void carDeleteCascadingSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", false);

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("yes")
       .out("Cascading\n")
       .out("Delete them [y/N] > ").in("yes");

    when(cars.findById(3)).thenReturn(mockCar);
    doThrow(new CascadingException("Cascading")).when(cars).deleteCar(5, 3, false);
    doNothing().when(cars).deleteCar(5, 3, true);
    when(session.getCurrentUserId()).thenReturn(5);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Done");

    cli.assertMatchesHistory();
    verify(cars).deleteCar(5, 3, false);
    verify(cars).deleteCar(5, 3, true);
  }

  @Test
  void carEditFailsWithNoArgument() {
    assertThatThrownBy(() -> car.editCar(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(cars);
  }

  @Test
  void carEditSuccess() {
    var oldCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);
    var newCar = new Car(3, "Brand", "Model", 2001, 2000000, "ok", true);

    cli.out("Brand (Brand) > ").in("")
       .out("Model (Model) > ").in("")
       .out("Production year (2001) > ").in("2001")
       .out("Price (1000000) > ").in("2000000")
       .out("Condition (poor) > ").in("ok");

    when(session.getCurrentUserId()).thenReturn(5);
    when(cars.findById(3)).thenReturn(oldCar);
    when(cars.editCar(5, 3, null, null, 2001, 2000000, "ok")).thenReturn(newCar);

    assertThat(car.editCar(session, cli, "3")).isEqualTo("Saved changes to " + newCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).findById(3);
    verify(cars).editCar(5, 3, null, null, 2001, 2000000, "ok");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void createCarSuccess() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor", true);

    cli.out("Brand > ").in("Brand")
       .out("Model > ").in("Model")
       .out("Production year > ").in("2001")
       .out("Price > ").in("1000000")
       .out("Condition > ").in("poor");

    when(session.getCurrentUserId()).thenReturn(6);
    when(cars.createCar(6, "Brand", "Model", 2001, 1000000, "poor")).thenReturn(mockCar);

    assertThat(car.createCar(session, cli)).isEqualTo("Created " + mockCar.prettyFormat());

    cli.assertMatchesHistory();
    verify(cars).createCar(6, "Brand", "Model", 2001, 1000000, "poor");
    verifyNoMoreInteractions(cars);
  }

  @Test
  void listCarsTest() {
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

    when(cars.lookupCars("", "", new IntRange(2000, 2010), new IntRange(100000,10000000), "o",
                         List.of(Availability.AVAILABLE, Availability.UNAVAILABLE), CarSorting.NAME_ASC))
        .thenReturn(List.of(dummyCars));

    var table = new TableFormatter("ID", "Brand", "Model", "Prod. year", "Price", "Condition", "Available for purchase");
    table.addRow(3, "Brand", "Model", 2001, 1000000, "poor", "No");
    table.addRow(64, "Toyota", "Corolla", 2009, 2000000, "ok", "Yes");

    assertThat(car.listCars(session, cli)).isEqualTo(table.format(true));

    cli.assertMatchesHistory();
  }
}
