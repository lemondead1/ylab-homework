package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;

public interface Controller {
  void registerEndpoints(TreeCommandBuilder<?> builder);
}
