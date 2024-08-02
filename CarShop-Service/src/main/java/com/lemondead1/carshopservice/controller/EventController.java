package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.TreeSubcommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.event.Event;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.DateRange;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EventController implements Controller {
  private final EventService events;

  @Override
  public void registerEndpoints(TreeSubcommandBuilder builder) {
    builder.push("event").describe("Use 'event' to access event database.").allow(UserRole.ADMIN)

           .accept("list", this::list)
           .describe("Use 'event list' to list events.")
           .allow(UserRole.ADMIN)
           .pop()

           .accept("dump", this::dump)
           .describe("Use 'event dump' to dump event log into a file.")
           .allow(UserRole.ADMIN)
           .pop()

           .pop();
  }

  String list(SessionService session, ConsoleIO cli, String... path) {
    var types = cli.parseOptional("Type > ", IdListParser.of(EventType.values())).orElse(EventType.ALL);
    var dateRange = cli.parseOptional("Date > ", DateRangeParser.INSTANCE).orElse(DateRange.ALL);
    var username = cli.parseOptional("User > ", StringParser.INSTANCE).orElse("");
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(EventSorting.class)).orElse(EventSorting.TIMESTAMP_DESC);
    var list = events.findEvents(types, dateRange, username, sorting);
    return list.stream().map(Event::serialize).collect(Collectors.joining("\n")) + "\nTotal rows: " + list.size();
  }

  String dump(SessionService session, ConsoleIO cli, String... path) {
    var types = cli.parseOptional("Type > ", IdListParser.of(EventType.values())).orElse(EventType.ALL);
    var dateRange = cli.parseOptional("Date > ", DateRangeParser.INSTANCE).orElse(DateRange.ALL);
    var username = cli.parseOptional("User > ", StringParser.INSTANCE).orElse("");
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(EventSorting.class)).orElse(EventSorting.TIMESTAMP_DESC);
    var file = cli.parse("File > ", FileParser.INSTANCE);
    events.dumpEvents(types, dateRange, username, sorting, file);
    return "Dumped event log into '" + file + "'";
  }
}
