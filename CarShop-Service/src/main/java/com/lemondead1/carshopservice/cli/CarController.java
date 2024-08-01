package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.cli.parsing.EnumParser;
import com.lemondead1.carshopservice.cli.parsing.IntParser;
import com.lemondead1.carshopservice.cli.parsing.StringParser;
import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.TableFormatter;

public class CarController implements Controller {
  private final CarService cars;

  public CarController(CarService cars) {
    this.cars = cars;
  }

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder
        .push("car", "Car CRUD operations").allow(UserRole.CLIENT, UserRole.MANAGER, UserRole.ADMIN)
        .accept("create", "Creates a new car", this::createCar).allow(UserRole.MANAGER, UserRole.ADMIN).pop()
        .accept("search", "Car search", this::listCars).allow(UserRole.CLIENT, UserRole.MANAGER, UserRole.ADMIN).pop();
  }

  String createCar(SessionService session, ConsoleIO cli, String... path) {
    var brand = cli.parse("Brand > ", StringParser.INSTANCE);
    var model = cli.parse("Model > ", StringParser.INSTANCE);
    var yearOfIssue = cli.parse("Year of issue > ", IntParser.INSTANCE);
    var price = cli.parse("Price > ", IntParser.INSTANCE);
    var condition = cli.parse("Condition > ", StringParser.INSTANCE);
    var id = cars.createCar(session.getCurrentUserId(), brand, model, yearOfIssue, price, condition);
    return "Created car with id " + id + ".";
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
}
