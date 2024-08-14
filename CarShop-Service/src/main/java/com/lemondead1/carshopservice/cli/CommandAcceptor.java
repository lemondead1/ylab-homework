package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.command.CommandTreeRoot;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.exceptions.CommandException;
import com.lemondead1.carshopservice.service.SessionService;
import lombok.RequiredArgsConstructor;

import java.util.function.BooleanSupplier;

/**
 * Polls console for input and passes it to rootCommand, then commits or rolls back database transactions.
 */
@RequiredArgsConstructor
public class CommandAcceptor {
  private final BooleanSupplier doContinue;
  private final CLI cli;
  private final SessionService session;
  private final CommandTreeRoot rootCommand;
  private final DBManager db;

  public void acceptCommands() {
    while (doContinue.getAsBoolean()) {
      var path = cli.readInteractive("> ");
      if (path.isEmpty()) {
        continue;
      }
      var split = path.split(" +");
      try {
        rootCommand.execute(session.getCurrentUser(), cli, split);
        db.commit();
      } catch (CommandException e) {
        db.rollback();
        cli.println(e.getMessage());
      } catch (RuntimeException e) {
        db.rollback();
        e.printStackTrace();
      }
    }
  }
}
