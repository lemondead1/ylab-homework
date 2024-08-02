package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.exceptions.WrongUsageException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.TableFormatter;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class OrderController implements Controller {
  private final OrderService orders;

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
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

           .accept("find", this::find)
           .describe("Use 'order find' to find orders.")
           .allow(MANAGER, ADMIN)
           .pop()

           .pop();

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

  String cancel(SessionService session, ConsoleIO cli, String... path) {
    if (path.length == 0) {
      return "Usage: order cancel <order id>";
    }
    int orderId = IntParser.INSTANCE.parse(path[0]);
    var order = orders.find(orderId);
    if (session.getCurrentUserRole() == CLIENT && order.customer().id() != session.getCurrentUserId()) {
      throw new CommandException("Wrong order id.");
    }
    orders.cancel(session.getCurrentUserId(), orderId);
    return "Cancelled " + order;
  }

  String updateState(SessionService session, ConsoleIO cli, String... path) {
    if (path.length < 2) {
      return "Usage: order update-state <order id> <new state>";
    }
    int orderId = IntParser.INSTANCE.parse(path[0]);
    var newState = IdParser.of(OrderState.class).parse(path[1]);
    var order = orders.find(orderId);
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
    var order = orders.find(orderId);
    cli.printf("Deleting %s\n", order);
    if (cli.parseOptional("Confirm [y/N] > ", BooleanParser.DEFAULT_TO_FALSE).orElse(false)) {
      orders.deleteOrder(session.getCurrentUserId(), orderId);
      return "Deleted";
    }
    return "Cancelled";
  }

  String find(SessionService session, ConsoleIO cli, String... path) {
    var username = cli.parseOptional("Customer > ", StringParser.INSTANCE).orElse("");
    var carBrand = cli.parseOptional("Car brand > ", StringParser.INSTANCE).orElse("");
    var carModel = cli.parseOptional("Car model > ", StringParser.INSTANCE).orElse("");
    var state = cli.parseOptional("State > ", IdListParser.of(OrderState.class)).orElse(OrderState.ALL);
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(OrderSorting.class)).orElse(OrderSorting.LATEST_FIRST);
    var list = orders.findAllOrders(username, carBrand, carModel, state, sorting);
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
}
