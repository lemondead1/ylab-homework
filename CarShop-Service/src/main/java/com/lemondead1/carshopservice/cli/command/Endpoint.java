package com.lemondead1.carshopservice.cli.command;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.service.SessionService;
import org.jetbrains.annotations.Nullable;

public interface Endpoint {
  @Nullable
  String execute(SessionService currentUserId, ConsoleIO cli, String[] parameters);
}
