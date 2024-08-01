package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventRepo {
  private final List<Event> events = new ArrayList<>();

  public void submitEvent(Event event) {
    events.add(event);
  }

  public List<Event> listEvents() {
    return Collections.unmodifiableList(events);
  }
}
