package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.service.SessionService;

public class OrderController implements Controller {
  private final OrderRepo orders;

  public OrderController(OrderRepo orders) {
    this.orders = orders;
  }

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {

  }
}
