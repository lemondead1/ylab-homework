package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.CLI;
import com.lemondead1.carshopservice.cli.command.builders.TreeCommandBuilder;
import com.lemondead1.carshopservice.cli.parsing.*;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.DateRange;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EventController {
  private final EventService events;

  public void registerEndpoints(TreeCommandBuilder<?> builder) {
    builder.push("event").describe("Use 'event' to access event database.").allow(UserRole.ADMIN)

           .accept("search", this::search)
           .describe("Use 'event search' to list events.")
           .allow(UserRole.ADMIN)
           .pop()

           .accept("dump", this::dump)
           .describe("Use 'event dump' to dump event log into a file.")
           .allow(UserRole.ADMIN)
           .pop()

           .pop();
  }

  String search(User currentUser, CLI cli, String... path) {
    var types = cli.parseOptional("Type > ", IdListParser.of(EventType.values())).orElse(EventType.ALL);
    var dateRange = cli.parseOptional("Date > ", DateRangeParser.INSTANCE).orElse(DateRange.ALL);
    var username = cli.parseOptional("User > ", StringParser.INSTANCE).orElse("");
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(EventSorting.class)).orElse(EventSorting.TIMESTAMP_DESC);
    var list = events.findEvents(types, dateRange, username, sorting);
    return list.stream().map(Event::json).collect(Collectors.joining("\n")) + "\nRow count: " + list.size();
  }

  String dump(User currentUser, CLI cli, String... path) {
    var types = cli.parseOptional("Type > ", IdListParser.of(EventType.values())).orElse(EventType.ALL);
    var dateRange = cli.parseOptional("Date > ", DateRangeParser.INSTANCE).orElse(DateRange.ALL);
    var username = cli.parseOptional("User > ", StringParser.INSTANCE).orElse("");
    var sorting = cli.parseOptional("Sorting > ", IdParser.of(EventSorting.class)).orElse(EventSorting.TIMESTAMP_DESC);
    var file = cli.parse("File > ", FileParser.INSTANCE);
    events.dumpEvents(types, dateRange, username, sorting, file);
    return "Dumped event log into '" + file + "'";
  }
}
