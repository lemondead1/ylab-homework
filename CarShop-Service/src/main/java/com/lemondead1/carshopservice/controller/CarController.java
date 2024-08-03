package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.Availability;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.exceptions.CascadingException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class CarController implements Controller {
  private final CarService cars;

  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
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
    var car = cars.createCar(session.getCurrentUserId(), brand, model, productionYear, price, condition);
    return "Created " + car.prettyFormat();
  }

  String listCars(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parseOptional("Brand > ", StringParser.INSTANCE).orElse("");
    var model = cli.parseOptional("Model > ", StringParser.INSTANCE).orElse("");
    var productionYear = cli.parseOptional("Prod. year > ", IntRangeParser.INSTANCE).orElse(IntRange.ALL);
    var price = cli.parseOptional("Price > ", IntRangeParser.INSTANCE).orElse(IntRange.ALL);
    var condition = cli.parseOptional("Condition > ", StringParser.INSTANCE).orElse("");
    var availability = cli.parseOptional("Availability for purchase > ", IdListParser.of(Availability.class))
                          .orElse(List.of(Availability.AVAILABLE));
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(CarSorting.class)).orElse(CarSorting.NAME_ASC);
    var list = cars.lookupCars(brand, model, productionYear, price, condition, availability, sorting);
    var table =
        new TableFormatter("ID", "Brand", "Model", "Prod. year", "Price", "Condition", "Available for purchase");
    for (var car : list) {
      table.addRow(car.id(), car.brand(), car.model(), car.productionYear(), car.price(), car.condition(),
                   car.availableForPurchase() == Availability.AVAILABLE ? "Yes" : "No");
    }
    return table.format(true);
  }

  String editCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    var newBrand = cli.parseOptional("Brand (" + car.brand() + ") > ", StringParser.INSTANCE).orElse(null);
    var newModel = cli.parseOptional("Model (" + car.model() + ") > ", StringParser.INSTANCE).orElse(null);
    var prodYear = cli.parseOptional("Production year (" + car.productionYear() + ") > ", IntParser.INSTANCE)
                      .orElse(null);
    var newPrice = cli.parseOptional("Price (" + car.price() + ") > ", IntParser.INSTANCE).orElse(null);
    var newCondition = cli.parseOptional("Condition (" + car.condition() + ") > ", StringParser.INSTANCE).orElse(null);
    car = cars.editCar(session.getCurrentUserId(), id, newBrand, newModel, prodYear, newPrice, newCondition);
    return "Saved changes to " + car.prettyFormat();
  }

  String deleteCar(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    int id = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(id);
    cli.printf("Deleting %s", car.prettyFormat());
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
