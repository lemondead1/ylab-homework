package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.annotations.Timed;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.util.Range;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for interfacing with the event database.
 * It provides convenience methods for both submitting and querying events.
 */
@Timed
@Slf4j
@RequiredArgsConstructor
public class EventService {
  private final EventRepo events;
  private final TimeService time;

  @Transactional
  public void postEvent(int userId, EventType eventType, Map<String, Object> data) {
    log.info("Posting a new event: userId={}, type={}, data={}.", userId, eventType, data);
    var now = time.now();
    events.create(now, userId, eventType, data);
  }

  public void onUserLoggedIn(int userId) {
    postEvent(userId, EventType.USER_LOGGED_IN, Map.of());
  }

  public void onUserSignedUp(User user) {
    postEvent(user.id(), EventType.USER_SIGNED_UP, Map.of("username", user.username(),
                                                          "phone_number", user.phoneNumber(),
                                                          "email", user.email()));
  }

  @Transactional
  public List<Event> findEvents(Collection<EventType> types,
                                Range<Instant> range,
                                String username,
                                EventSorting sorting) {
    return events.lookup(EnumSet.copyOf(types), range, username, sorting);
  }
}
