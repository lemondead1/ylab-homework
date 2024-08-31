package com.lemondead1.carshopservice.service;

import com.lemondead1.audit.Auditor;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface EventService extends Auditor {
  void onUserLoggedIn(int userId);

  void onUserSignedUp(User user);

  List<Event> findEvents(Collection<EventType> types, Range<Instant> range, String username, EventSorting sorting);
}
