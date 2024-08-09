package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.CommandTreeRoot;
import com.lemondead1.carshopservice.service.SessionService;
import lombok.RequiredArgsConstructor;

import java.util.function.BooleanSupplier;

/**
 * Polls console for input and passes it to rootCommand
 */
@RequiredArgsConstructor
public class CommandAcceptor {
  private final BooleanSupplier doContinue;
  private final ConsoleIO cli;
  private final SessionService session;
  private final CommandTreeRoot rootCommand;

  public void acceptCommands() {
    while (doContinue.getAsBoolean()) {
      var path = cli.readInteractive("> ");
      if (path.isEmpty()) {
        continue;
      }
      var split = path.split(" +");
      try {
        rootCommand.execute(session.getCurrentUser(), cli, split);
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}
