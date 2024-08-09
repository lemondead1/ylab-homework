package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.entity.User;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class HomeController implements Controller {
  private final Runnable exit;

  @Override
  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.accept("exit", this::exit)
           .describe("Use 'exit' to close the app.")
           .allow(CLIENT, MANAGER, ADMIN, ANONYMOUS)
           .pop();
  }

  String exit(User currentUser, ConsoleIO cli, String... params) {
    exit.run();
    return "Goodbye!";
  }
}
