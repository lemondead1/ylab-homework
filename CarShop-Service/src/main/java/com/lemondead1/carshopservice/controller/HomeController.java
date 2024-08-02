package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.CarShopServiceApplication;
import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.service.SessionService;

import static com.lemondead1.carshopservice.enums.UserRole.*;

public class HomeController implements Controller {
  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.accept("exit", this::exit)
           .describe("Use 'exit' to close the app.")
           .allow(CLIENT, MANAGER, ADMIN, ANONYMOUS)
           .pop();
  }

  String exit(SessionService session, ConsoleIO cli, String... params) {
    CarShopServiceApplication.setExited(true);
    return "Goodbye!";
  }
}
