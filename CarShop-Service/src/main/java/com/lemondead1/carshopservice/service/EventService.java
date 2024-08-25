package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EventService {
  void postEvent(int userId, EventType eventType, Map<String, Object> data);

  void onUserLoggedIn(int userId);

  void onUserSignedUp(User user);

  List<Event> findEvents(Collection<EventType> types, Range<Instant> range, String username, EventSorting sorting);
}
