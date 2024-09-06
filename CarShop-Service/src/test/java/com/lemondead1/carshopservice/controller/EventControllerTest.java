package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.config.EnvironmentConfig;
import com.lemondead1.carshopservice.dto.event.EventQueryDTO;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {
  @Mock
  EventService eventService;

  ObjectMapper objectMapper = EnvironmentConfig.objectMapper();

  MapStruct mapStruct = new MapStructImpl();

  EventController controller;

  MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    controller = new EventController(eventService, mapStruct);
    mockMvc = standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                         .build();
  }

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
