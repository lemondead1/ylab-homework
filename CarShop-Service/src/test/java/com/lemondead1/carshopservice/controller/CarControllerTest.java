package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.Car;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
  void carDeleteFailsWhenNoParameterIsPresent() {
    assertThatThrownBy(() -> car.deleteCar(session, cli)).isInstanceOf(WrongUsageException.class);

    cli.assertMatchesHistory();
    verifyNoInteractions(users);
  }

  @Test
  void carDeleteCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

    cli.out("Deleting \"Brand\" \"Model\" of 2001 p/y priced 1000000 in \"poor\" condition with id 3")
       .out("Confirm [y/N] > ").in("not");

    when(cars.findById(3)).thenReturn(mockCar);

    assertThat(car.deleteCar(session, cli, "3")).isEqualTo("Cancelled");

    cli.assertMatchesHistory();
  }

  @Test
  void carDeleteCascadeCancelled() {
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

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
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

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
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

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
    var oldCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");
    var newCar = new Car(3, "Brand", "Model", 2001, 2000000, "ok");

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
    var mockCar = new Car(3, "Brand", "Model", 2001, 1000000, "poor");

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
}
