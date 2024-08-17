package com.lemondead1.carshopservice.servlet.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.event.EventQueryDTO;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@WebServlet(value = "/events/search", asyncSupported = true)
@ServletSecurity(@HttpConstraint(rolesAllowed = "admin"))
@RequiredArgsConstructor
public class EventSearchServlet extends HttpServlet {
  private final EventService eventService;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var query = objectMapper.readValue(req.getInputStream(), EventQueryDTO.class);

    var result = eventService.findEvents(
        coalesce(query.types(), EventType.ALL),
        coalesce(query.dates(), Range.all()),
        coalesce(query.username(), ""),
        coalesce(query.sorting(), EventSorting.TIMESTAMP_DESC)
    );

    resp.setContentType("application/json");
    var resultDto = mapStruct.eventListToDtoList(result);
    objectMapper.writeValue(resp.getOutputStream(), resultDto);
  }
}
