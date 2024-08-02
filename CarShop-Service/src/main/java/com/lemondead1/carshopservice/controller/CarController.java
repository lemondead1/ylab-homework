package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class CarController implements Controller {
  private final CarService cars;

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.push("car").describe("Use 'car' to access car database.").allow(CLIENT, MANAGER, ADMIN)

           .accept("create", this::createCar)
           .describe("Use 'car create' to create a new car.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("search", this::listCars)
           .describe("Use 'car search' to look up cars.")
           .allow(CLIENT, MANAGER, ADMIN)
           .pop()

           .accept("edit", this::editCar)
           .describe("Use 'car edit' to update car info.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("delete", this::deleteCar)
           .describe("Use 'car delete' to delete a car.")
           .allow(MANAGER, ADMIN)
           .pop()

           .pop();
  }

  String createCar(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parse("Brand > ", StringParser.INSTANCE);
    var model = cli.parse("Model > ", StringParser.INSTANCE);
    var productionYear = cli.parse("Production year > ", IntParser.INSTANCE);
    var price = cli.parse("Price > ", IntParser.INSTANCE);
    var condition = cli.parse("Condition > ", StringParser.INSTANCE);
    var id = cars.createCar(session.getCurrentUserId(), brand, model, productionYear, price, condition);
    return "Created a car with id " + id + ".";
  }

  String listCars(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parseOptional("Brand > ", StringParser.INSTANCE).orElse(null);
    var model = cli.parseOptional("Model > ", StringParser.INSTANCE).orElse(null);
    var productionYear = cli.parseOptional("Production year > ", IntRangeParser.INSTANCE).orElse(null);
    var price = cli.parseOptional("Price > ", IntRangeParser.INSTANCE).orElse(null);
    //TODO Not sure whether parsing this way is any useful. Maybe I should make it an enum
    var condition = cli.parseOptional("Condition > ", StringParser.INSTANCE).orElse(null);
    var sorting = cli.parseOptional("Sort by > ", IdParser.of(CarSorting.class)).orElse(CarSorting.NAME_ASC);
    var list = cars.lookupCars(brand, model, productionYear, price, condition, sorting);
    var table = new TableFormatter("ID", "Brand", "Model", "Prod. year", "Price", "Condition");
    for (var car : list) {
      table.addRow(car.id(), car.brand(), car.model(), car.productionYear(), car.price(), car.condition());
    }
    return table.format(true);
  }

  String editCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new CommandException("Usage: car edit <id>");
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    var newBrand = cli.parseOptional("Brand (" + car.brand() + ") > ", StringParser.INSTANCE).orElse(null);
    var newModel = cli.parseOptional("Model (" + car.model() + ") > ", StringParser.INSTANCE).orElse(null);
    var prodYear = cli.parseOptional("Production year (" + car.productionYear() + ") > ", IntParser.INSTANCE)
                      .orElse(null);
    var newPrice = cli.parseOptional("Price (" + car.price() + ") > ", IntParser.INSTANCE).orElse(null);
    var newCondition = cli.parseOptional("Condition (" + car.condition() + ") > ", StringParser.INSTANCE).orElse(null);
    cars.editCar(session.getCurrentUserId(), id, newBrand, newModel, prodYear, newPrice, newCondition);
    return "Saved changes to the car '" + id + "'";
  }

  String deleteCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new CommandException("Usage: car delete <id>");
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    cli.printf("Deleting car: Brand=%s, Model=%s, Prod. year=%s, Price=%s, Condition=%s.",
               car.brand(), car.model(), car.productionYear(), car.price(), car.price(), car.condition());
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
