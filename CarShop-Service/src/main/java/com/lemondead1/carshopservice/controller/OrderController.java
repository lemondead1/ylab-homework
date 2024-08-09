package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.CarService;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class OrderController implements Controller {
  private final OrderService orders;
  private final CarService cars;
  private final UserService users;

  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.push("order").describe("Commands for managing orders").allow(CLIENT, MANAGER, ADMIN)

           .accept("purchase", this::purchase)
           .describe("Adds a new car purchase order")
           .allow(ADMIN, CLIENT, MANAGER)
           .pop()

           .accept("service", this::service)
           .describe("Use 'order service <car id> to schedule service for your car")
           .allow(CLIENT, MANAGER, ADMIN)
           .pop()

           .accept("my-list", this::myOrders)
           .describe("Use 'order my-list [sorting]' to list your orders")
           .allow(CLIENT, MANAGER, ADMIN)
           .pop()

           .accept("cancel", this::cancel)
           .describe("Use 'order cancel <order id>' to cancel orders.")
           .allow(CLIENT, MANAGER, ADMIN)
           .pop()

           .accept("delete", this::deleteOrder)
           .describe("Use 'order delete <order id>' to delete orders.")
           .allow(ADMIN)
           .pop()

           .accept("update-state", this::updateState)
           .describe("Use 'order update-state <order id> <new state>' to change order state.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("search", this::search)
           .describe("Use 'order search' to search orders.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("create", this::create)
           .describe("Use 'order create <customer id> <car id>' to create order.")
           .allow(MANAGER, ADMIN)
           .pop()

           .accept("by-id", this::byId)
           .describe("Use 'order by-id <id>' to lookup orders by id.")
           .allow(MANAGER, ADMIN)
           .pop()

           .pop();

  }

  String byId(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    var order = IntParser.INSTANCE.map(orders::findById).parse(path[0]);
    return "Found " + order.prettyFormat();
  }

  String purchase(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    int carId = IntParser.INSTANCE.parse(path[0]);
    var car = cars.findById(carId);
    var comments = cli.parseOptional("Comments > ", StringParser.INSTANCE).orElse("");
    cli.printf("Ordering %s.\n", car.prettyFormat());
    if (!cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }
    orders.purchase(session.getCurrentUserId(), carId, comments);
    return "Ordered " + car.prettyFormat();
  }

  String service(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    int carId = IntParser.INSTANCE.parse(path[0]);
    var comments = cli.parseOptional("Comments > ", StringParser.INSTANCE).orElse("");
    var order = orders.orderService(session.getCurrentUserId(), carId, comments);
    return "Scheduled service for " + order.car().prettyFormat();
  }

  String cancel(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    var order = IntParser.INSTANCE.map(orders::findById).parse(path[0]);
    if (session.getCurrentUserRole() == CLIENT && order.customer().id() != session.getCurrentUserId()) {
      throw new CommandException("Wrong order id.");
    }
    orders.cancel(session.getCurrentUserId(), order.id());
    return "Cancelled " + order.prettyFormat();
  }

  String updateState(SessionService session, ConsoleIO cli, String... path) {
    if (path.length < 2) {
      throw new WrongUsageException();
    }
    int orderId = IntParser.INSTANCE.parse(path[0]);
    var newState = IdParser.of(OrderState.class).parse(path[1]);
    var order = orders.findById(orderId);
    if (order.state() == newState) {
      throw new CommandException("State has not been changed.");
    }
    var addedComments = cli.parseOptional("Append comment > ", StringParser.INSTANCE).map(c -> "\n" + c).orElse("");
    orders.updateState(session.getCurrentUserId(), orderId, newState, addedComments);
    return "Done";
  }

  String myOrders(SessionService session, ConsoleIO cli, String... path) {
    OrderSorting sorting = OrderSorting.LATEST_FIRST;
    if (path.length > 0) {
      sorting = IdParser.of(OrderSorting.class).parse(path[0]);
    }
    var list = orders.findMyOrders(session.getCurrentUserId(), sorting);
    var table = new TableFormatter("Order ID", "Creation date", "Type", "Status",
                                   "Car ID", "Car brand", "Car model", "Comments");
    for (var row : list) {
      table.addRow(row.id(), row.createdAt(), row.type().getPrettyName(), row.state().getPrettyName(),
                   row.car().id(), row.car().brand(), row.car().model(), row.comments());
    }
    return table.format(true);
  }

  String deleteOrder(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      throw new WrongUsageException();
    }
    int orderId = IntParser.INSTANCE.parse(path[0]);
    var order = orders.findById(orderId);
    cli.printf("Deleting %s\n", order.prettyFormat());

    if (!cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      return "Cancelled";
    }

    orders.deleteOrder(session.getCurrentUserId(), orderId);
    return "Deleted";
  }

  String search(SessionService session, ConsoleIO cli, String... path) {
    var dates = cli.parseOptional("Date > ", DateRangeParser.INSTANCE).orElse(DateRange.ALL);
    var kind = cli.parseOptional("Kind > ", IdListParser.of(OrderKind.class)).orElse(OrderKind.ALL);
    var username = cli.parseOptional("Customer > ", StringParser.INSTANCE).orElse("");
    var carBrand = cli.parseOptional("Car brand > ", StringParser.INSTANCE).orElse("");
    var carModel = cli.parseOptional("Car model > ", StringParser.INSTANCE).orElse("");
    var state = cli.parseOptional("State > ", IdListParser.of(OrderState.class)).orElse(OrderState.ALL);
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(OrderSorting.class)).orElse(OrderSorting.LATEST_FIRST);
    var list = orders.lookupOrders(dates, username, carBrand, carModel, kind, state, sorting);
    var table = new TableFormatter("Order ID", "Creation date", "Type", "Status",
                                   "Customer ID", "Customer name", "Car ID", "Car brand", "Car model",
                                   "Comments");
    for (var row : list) {
      table.addRow(row.id(), row.createdAt(), row.type().getPrettyName(), row.state().getPrettyName(),
                   row.customer().id(), row.customer().username(), row.car().id(), row.car().brand(), row.car().model(),
                   row.comments());
    }
    return table.format(true);
  }

  String create(SessionService session, ConsoleIO cli, String... path) {
    if (path.length < 2) {
      throw new WrongUsageException();
    }
    var customer = IntParser.INSTANCE.map(users::findById).parse(path[0]);
    var car = IntParser.INSTANCE.map(cars::findById).parse(path[1]);
    var kind = cli.parse("Kind > ", IdParser.of(OrderKind.class));
    var state = cli.parseOptional("State > ", IdParser.of(OrderState.class)).orElse(OrderState.NEW);
    var comment = cli.parseOptional("Comments > ", StringParser.INSTANCE).orElse("");
    var order = orders.createOrder(session.getCurrentUserId(), customer.id(), car.id(), kind, state, comment);
    return "Created " + order.prettyFormat() + ".";
  }
}
