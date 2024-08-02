package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.TableFormatter;

import static com.lemondead1.carshopservice.enums.UserRole.*;

public class OrderController implements Controller {
  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.push("order", "Commands for managing orders").allow(CLIENT, MANAGER, ADMIN)
           .accept("purchase", "Adds a new car purchase order", this::purchase).allow(ADMIN, CLIENT, MANAGER).pop()
           .accept("service",
                   "Use 'order service <car id> to schedule service for your car",
                   this::service).allow(CLIENT, MANAGER, ADMIN).pop()
           .accept("my",
                   "Use 'order my [sorting]' to list your orders",
                   this::myOrders).allow(CLIENT, MANAGER, ADMIN).pop()
           .accept("find", "Use 'order find' to find orders.", this::find).allow(MANAGER, ADMIN).pop();

  }

  String purchase(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      return "Usage: order purchase <car id>";
    }
    int carId = IntParser.INSTANCE.parse(path[0]);
    var comments = cli.parseOptional("Comments > ", StringParser.INSTANCE).orElse("");
    if (!cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }
    var car = orders.createPurchaseOrder(session.getCurrentUserId(), carId, comments);
    return "Ordered " + car;
  }

  String service(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      return "Usage: order service <car id>";
    }
    int carId = IntParser.INSTANCE.parse(path[0]);
    var comments = cli.parseOptional("Comments > ", StringParser.INSTANCE).orElse("");
    var car = orders.createServiceOrder(session.getCurrentUserId(), carId, comments);
    return "Scheduled service for " + car;
  }

  String myOrders(SessionService session, ConsoleIO cli, String... path) {
    OrderSorting sorting = OrderSorting.LATEST_FIRST;
    if (path.length > 0) {
      sorting = EnumParser.of(OrderSorting.class).parse(path[0]);
    }
    var list = orders.findMyOrders(session.getCurrentUserId(), sorting);
    var table = new TableFormatter("Order ID", "Creation date", "Type", "Status",
                                   "Car ID", "Car brand", "Car model", "Comments");
    for (var row : list) {
      table.addRow(row.id(), row.createdAt(), row.type().getPrettyName(), row.state().getPrettyName(),
                   row.car().id(), row.car().brand(), row.car().model(), row.comments());
    }
    return table.format() + "\nRow count: " + list.size();
  }

  String find(SessionService session, ConsoleIO cli, String... path) {
    var username = cli.parseOptional("Customer username > ", StringParser.INSTANCE).orElse(null);
    var carBrand = cli.parseOptional("Car brand > ", StringParser.INSTANCE).orElse(null);
    var carModel = cli.parseOptional("Car model > ", StringParser.INSTANCE).orElse(null);
    var sorting = cli.parseOptional("Sorting > ", EnumParser.of(OrderSorting.class)).orElse(null);
    var list = orders.findAllOrders(username, carBrand, carModel, sorting);
    var table = new TableFormatter("Order ID", "Creation date", "Type", "Status",
                                   "Customer ID", "Customer name", "Car ID", "Car brand", "Car model",
                                   "Comments");
    for (var row : list) {
      table.addRow(row.id(), row.createdAt(), row.type().getPrettyName(), row.state().getPrettyName(),
                   row.customer().id(), row.customer().username(), row.car().id(), row.car().brand(), row.car().model(),
                   row.comments());
    }
    return table.format() + "\nRow count: " + list.size();
  }
}
