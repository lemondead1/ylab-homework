package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.config.WebConfig;
import com.lemondead1.carshopservice.conversion.MapStruct;
import com.lemondead1.carshopservice.dto.event.EventQueryDTO;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ComponentScan({ "com.lemondead1.carshopservice.conversion" })
@ContextConfiguration(classes = { WebConfig.class, EventController.class })
@AutoConfigureMockMvc
public class EventControllerTest {
  @MockBean
  EventService eventService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  MapStruct mapStruct;

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("POST /event/search calls EventService.findEvents and responds with the result.")
  public void searchEventsTest() throws Exception {
    var query = new EventQueryDTO(List.of(EventType.CAR_CREATED), null, "user", null);
    List<Event> result = List.of(new Event(10, Instant.now(), 3, EventType.CAR_CREATED, Map.of("a", "b")));
    var requestBody = objectMapper.writeValueAsString(query);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.eventListToDtoList(result));

    when(eventService.findEvents(List.of(EventType.CAR_CREATED), Range.all(), "user", EventSorting.TIMESTAMP_ASC))
        .thenReturn(result);

    mockMvc.perform(post("/events/search").content(requestBody)
                                          .contentType("application/json")
                                          .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(eventService).findEvents(List.of(EventType.CAR_CREATED), Range.all(), "user", EventSorting.TIMESTAMP_ASC);
  }
}
