package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.TableFormatter;

import static com.lemondead1.carshopservice.enums.UserRole.*;

public class CarController implements Controller {
  private final CarService cars;

  public CarController(CarService cars) {
    this.cars = cars;
  }

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder
        .push("car", "Car CRUD operations").allow(CLIENT, MANAGER, ADMIN)
        .accept("create", "Creates a new car", this::createCar).allow(MANAGER, ADMIN).pop()
        .accept("search", "Car search", this::listCars).allow(CLIENT, MANAGER, ADMIN).pop()
        .accept("edit", "Edit car", this::editCar).allow(MANAGER, ADMIN).pop()
        .accept("delete", "Deletes the car", this::deleteCar).allow(MANAGER, ADMIN).pop()
        .pop();
  }

  String createCar(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parse("Brand > ", StringParser.INSTANCE);
    var model = cli.parse("Model > ", StringParser.INSTANCE);
    var yearOfIssue = cli.parse("Year of issue > ", IntParser.INSTANCE);
    var price = cli.parse("Price > ", IntParser.INSTANCE);
    var condition = cli.parse("Condition > ", StringParser.INSTANCE);
    var id = cars.createCar(session.getCurrentUserId(), brand, model, yearOfIssue, price, condition);
    return "Created a car with id " + id + ".";
  }

  String listCars(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parseOptional("Brand (optional) > ", StringParser.INSTANCE);
    var model = cli.parseOptional("Model (optional) > ", StringParser.INSTANCE);
    var yearOfIssue = cli.parseOptional("Year of issue (optional) > ", IntParser.INSTANCE);
    var price = cli.parseOptional("Price (optional) > ", IntParser.INSTANCE);
    //TODO Not sure whether parsing this way is any useful. Maybe I should make it an enum
    var condition = cli.parseOptional("Condition (optional) > ", StringParser.INSTANCE);
    var sorting = cli.parseOptional("Sort by (optional) > ", EnumParser.of(CarSorting.class));
    var list = cars.lookupCars(brand.orElse(null),
                               model.orElse(null),
                               yearOfIssue.orElse(null),
                               price.orElse(null),
                               condition.orElse(null),
                               sorting.orElse(CarSorting.NAME_ASC));
    var table = new TableFormatter("ID", "Brand", "Model", "Year of issue", "Price", "Condition");
    for (var car : list) {
      table.addRow(car.id(), car.brand(), car.model(), car.yearOfIssue(), car.price(), car.condition());
    }
    return table.format();
  }

  String editCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new CommandException("Usage: car edit <id>");
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    var newBrand = cli.parseOptional("Brand (" + car.brand() + ") > ", StringParser.INSTANCE).orElse(car.brand());
    var newModel = cli.parseOptional("Model (" + car.model() + ") > ", StringParser.INSTANCE).orElse(car.model());
    var newYearOfIssue = cli.parse("Year of issue (" + car.yearOfIssue() + ") > ", IntParser.INSTANCE);
    var newPrice = cli.parse("Price (" + car.price() + ") > ", IntParser.INSTANCE);
    var newCondition = cli.parse("Condition (" + car.condition() + ") > ", StringParser.INSTANCE);
    cars.editCar(session.getCurrentUserId(), id, newBrand, newModel, newYearOfIssue, newPrice, newCondition);
    return "Saved changes to the car '" + id + "'";
  }

  String deleteCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new CommandException("Usage: car delete <id>");
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    cli.printf("Deleting car: Brand=%s, Model=%s, Year of issue=%s, Price=%s, Condition=%s.",
               car.brand(), car.model(), car.yearOfIssue(), car.price(), car.price(), car.condition());
    if (!cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }
    try {
      cars.deleteCar(session.getCurrentUserId(), id, false);
      return "Done";
    } catch (CascadingException e) {
      cli.println(e.getMessage());
    }
    if (!cli.parseOptional("Delete them [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }
    cars.deleteCar(session.getCurrentUserId(), id, true);
    return "Done";
  }
}
