package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.entity.User;
import lombok.RequiredArgsConstructor;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@RequiredArgsConstructor
public class HomeController {
  private final Runnable exit;

  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.accept("exit", this::exit)
           .describe("Use 'exit' to close the app.")
           .allow(CLIENT, MANAGER, ADMIN, ANONYMOUS)
           .pop();
  }

  String exit(User currentUser, CLI cli, String... params) {
    exit.run();
    return "Goodbye!";
  }
}
