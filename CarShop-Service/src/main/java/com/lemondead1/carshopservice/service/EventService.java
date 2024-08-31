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
  /**
   * Called when a user logs in.
   */
  void onUserLoggedIn(int userId);

  /**
   * Called when a user signs up.
   */
  void onUserSignedUp(User user);

  /**
   * Searches for events matching arguments.
   *
   * @param types    Event type filter.
   * @param range    Event date range.
   * @param username Username query.
   * @param sorting  Sorting.
   * @return List of events matching arguments.
   */
  List<Event> findEvents(Collection<EventType> types, Range<Instant> range, String username, EventSorting sorting);
}
