package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.event.Event;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.Setter;

import java.util.*;

public class EventRepo {
  @Setter
  private UserRepo users;

  private final List<Event> events = new ArrayList<>();

  public void submitEvent(Event event) {
    events.add(event);
  }

  public List<Event> listEvents() {
    return Collections.unmodifiableList(events);
  }

  private String findUsername(int user) {
    try {
      return users.findById(user).username();
    } catch (RowNotFoundException ex) {
      return "REMOVED";
    }
  }

  public List<Event> lookupEvents(Set<EventType> types, DateRange dates, String username, EventSorting sorting) {
    var stream = events.stream();
    stream = stream.filter(e -> types.contains(e.getType()));
    stream = stream.filter(e -> dates.test(e.getTimestamp()));
    stream = stream.filter(e -> StringUtil.containsIgnoreCase(findUsername(e.getUserId()), username));
    return stream.sorted(switch (sorting) {
      case TIMESTAMP_DESC -> Comparator.comparing(Event::getTimestamp).reversed();
      case TIMESTAMP_ASC -> Comparator.comparing(Event::getTimestamp);
      case USERNAME_ASC -> Comparator.comparing(e -> findUsername(e.getUserId()), String::compareToIgnoreCase);
      case USERNAME_DESC -> Comparator.comparing((Event e) -> findUsername(e.getUserId()), String::compareToIgnoreCase)
                                      .reversed();
      case TYPE_ASC -> Comparator.comparing(Event::getType);
      case TYPE_DESC -> Comparator.comparing(Event::getType).reversed();
    }).toList();
  }
}
