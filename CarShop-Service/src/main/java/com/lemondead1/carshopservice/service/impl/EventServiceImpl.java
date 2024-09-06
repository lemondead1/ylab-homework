package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.carshopservice.annotations.Timed;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.Range;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
  private final EventRepo events;

  @Transactional
  @Override
  public void postEvent(int userId, EventType eventType, Map<String, Object> data) {
    log.info("Posting a new event: userId={}, type={}, data={}.", userId, eventType, data);
    events.create(Instant.now(), userId, eventType, data);
  }

  @Override
  public void onUserLoggedIn(int userId) {
    postEvent(userId, EventType.USER_LOGGED_IN, Map.of());
  }

  @Override
  public void onUserSignedUp(User user) {
    postEvent(user.id(), EventType.USER_SIGNED_UP, Map.of("username", user.username(),
                                                          "phone_number", user.phoneNumber(),
                                                          "email", user.email()));
  }

  @Transactional
  @Override
  public List<Event> findEvents(Collection<EventType> types,
                                Range<Instant> range,
                                String username,
                                EventSorting sorting) {
    return events.lookup(EnumSet.copyOf(types), range, username, sorting);
  }
}
