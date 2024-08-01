package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.CarShopServiceApplication;
import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.ConsoleIO;
import com.lemondead1.carshopservice.service.SessionService;

public class HomeController implements Controller {
  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.accept("exit", "Closes the app", this::exit).pop();
  }

  String exit(SessionService session, ConsoleIO cli, String... params) {
    CarShopServiceApplication.setExited(true);
    return "Goodbye!";
  }
}
