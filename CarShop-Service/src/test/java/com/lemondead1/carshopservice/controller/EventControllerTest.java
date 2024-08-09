package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.*;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {
  @Mock
  EventService events;

  @Mock
  SessionService session;

  MockConsoleIO cli;

  EventController event;

  @BeforeEach
  void setup() {
    event = new EventController(events);

    cli = new MockConsoleIO();
  }

  @Test
  void eventControllerSearchReturnsSerializedEvents() {
    var now = Instant.now();
    var event = new Event(6, now, 6, EventType.USER_DELETED, "{\"abacaba\": \"something\"}");
    when(events.findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC)).thenReturn(List.of(event));

    cli.out("Type > ").in("")
       .out("Date > ").in("")
       .out("User > ").in("")
       .out("Sorting > ").in("newer_first");

    assertThat(this.event.search(session, cli)).isEqualTo(event.json() + "\nRow count: 1");

    cli.assertMatchesHistory();
    verify(events).findEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC);
  }

  @Test
  void eventControllerDumpsSerializedEvents() {
    cli.out("Type > ").in("")
       .out("Date > ").in("")
       .out("User > ").in("")
       .out("Sorting > ").in("newer_first")
       .out("File > ").in("events.txt");

    assertThat(event.dump(session, cli)).startsWith("Dumped event log into ");

    cli.assertMatchesHistory();

    verify(events).dumpEvents(EventType.ALL, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC,
                              Path.of("events.txt").toAbsolutePath());
  }
}
