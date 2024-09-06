package com.lemondead1.carshopservice.servlet.events;

import com.lemondead1.carshopservice.dto.event.EventQueryDTO;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static com.lemondead1.carshopservice.SharedTestObjects.mapStruct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventSearchServletTest extends ServletTest {
  @Mock
  EventService eventService;

  EventSearchServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new EventSearchServlet(eventService, jackson, mapStruct);
  }

  @Test
  @DisplayName("doPost calls lookupEvents and writes the result.")
  void doPostCallsLookupEventsAndWrites() throws IOException {
    var events = List.of(new Event(1, Instant.now(), 5, EventType.USER_CREATED, Map.of("some", "thing")),
                         new Event(2, Instant.now(), 7, EventType.USER_LOGGED_IN, Map.of("another", "thing")));
    var query = new EventQueryDTO(null, null, "name", EventSorting.TYPE_ASC);

    var requestBody = jackson.writeValueAsString(query);

    when(eventService.findEvents(EventType.ALL, Range.all(), "name", EventSorting.TYPE_ASC)).thenReturn(events);
    mockReqResp(null, true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(eventService).findEvents(EventType.ALL, Range.all(), "name", EventSorting.TYPE_ASC);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.eventListToDtoList(events)));
  }
}
