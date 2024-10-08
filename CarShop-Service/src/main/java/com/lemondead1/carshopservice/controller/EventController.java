package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.conversion.MapStruct;
import com.lemondead1.carshopservice.dto.event.EventDTO;
import com.lemondead1.carshopservice.dto.event.EventQueryDTO;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;
  private final MapStruct mapStruct;

  @PostMapping("/search")
  @Operation(summary = "Searches for events matching query.",
             description = "Searches for events matching query. Only available to admins.")
  @ApiResponse(responseCode = "200", description = "Search completed successfully.")
  public List<EventDTO> search(@RequestBody EventQueryDTO queryDTO) {
    List<Event> result = eventService.findEvents(
        coalesce(queryDTO.types(), EventType.ALL),
        coalesce(queryDTO.dates(), Range.all()),
        coalesce(queryDTO.username(), ""),
        coalesce(queryDTO.sorting(), EventSorting.TIMESTAMP_ASC)
    );
    return mapStruct.eventListToDtoList(result);
  }
}
