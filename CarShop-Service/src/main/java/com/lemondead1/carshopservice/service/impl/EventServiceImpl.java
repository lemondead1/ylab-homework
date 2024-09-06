package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.audit.Auditor;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.logging.annotations.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for interfacing with the event database.
 * It provides convenience methods for both submitting and querying events as well as implements {@linkplain Auditor}.
 */
@Timed
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
  private final EventRepo events;

  @Transactional
  @Override
  public void postEvent(String eventTypeId, Map<String, Object> data) {
    var currentRequest = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (currentRequest == null) {
      throw new IllegalStateException("No request was found.");
    }

    var currentUser = (User) currentRequest.getRequest().getUserPrincipal();
    if (currentUser == null) {
      throw new IllegalStateException("No user is authenticated.");
    }

    log.info("Posting a new event: userId={}, type={}, data={}.", currentUser.id(), eventTypeId, data);

    var eventType = EventType.parse(eventTypeId);
    events.create(Instant.now(), currentUser.id(), eventType, data);
  }

  /**
   * Called when a user logs in.
   */
  @Override
  public void onUserLoggedIn(int userId) {
    log.info("User #{} logged in.", userId);
    events.create(Instant.now(), userId, EventType.USER_LOGGED_IN, Map.of());
  }

  /**
   * Called when a user signs up.
   */
  @Override
  public void onUserSignedUp(User user) {
    log.info("User id={}, phone number={}, email={} signed up.", user.id(), user.phoneNumber(), user.email());
    events.create(Instant.now(), user.id(), EventType.USER_SIGNED_UP,
                  Map.of("username", user.username(), "phone_number", user.phoneNumber(), "email", user.email()));
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
