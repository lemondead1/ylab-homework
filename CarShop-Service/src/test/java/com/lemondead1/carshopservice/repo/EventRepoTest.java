package com.lemondead1.carshopservice.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.*;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EventRepoTest {
  private static final EventRepo events = new EventRepo(TestDBConnector.DB_MANAGER, SharedTestObjects.jackson);
  private static final UserRepo users = new UserRepo(TestDBConnector.DB_MANAGER);
  private static final ObjectMapper jackson = SharedTestObjects.jackson;

  @BeforeEach
  void beforeEach() {
    TestDBConnector.beforeEach();
  }

  @AfterEach
  void afterEach() {
    TestDBConnector.afterEach();
  }

  @Test
  @DisplayName("create creates an event.")
  void createEventTest() throws JsonProcessingException {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var json = jackson.readValue("{\"something\": \"doesnt_matter\", \"something_else\": \"value\"}",
                                 new TypeReference<Map<String, Object>>() { });
    var created = events.create(now, 1, EventType.USER_CREATED, json);
    assertThat(created)
        .isEqualTo(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_DESC).get(0))
        .isEqualTo(new Event(created.getId(), now, 1, EventType.USER_CREATED, json));
  }

  @ParameterizedTest
  @CsvSource({
      "'225, 227, 238, 241, 261, 262, 263', user_edited, 3.7.2014 - 10.7.2014, ''",
      "'240, 255',                          ALL,         3.7.2014 - 10.7.2014, admin",
  })
  @DisplayName("lookup returns entries matching arguments.")
  void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds,
                  @ConvertWith(HasIdEnumSetConverter.class) Set<EventType> types,
                  @ConvertWith(RangeConverter.class) Range<Instant> dates,
                  String username) {
    assertThat(events.lookup(types, dates, username, EventSorting.USERNAME_DESC))
        .map(Event::getId).containsExactlyInAnyOrder(expectedIds);
  }

  @Test
  void testSortingTimestampDesc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_DESC))
        .isSortedAccordingTo(Comparator.comparing(Event::getTimestamp).reversed())
        .hasSize(970);
  }

  @Test
  void testSortingTimestampAsc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_ASC))
        .isSortedAccordingTo(Comparator.comparing(Event::getTimestamp))
        .hasSize(970);
  }

  String getUsername(Event event) {
    try {
      return users.findById(event.getUserId()).username();
    } catch (NotFoundException e) {
      return "removed";
    }
  }

  @Test
  void testSortingUsernameAsc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.USERNAME_ASC))
        .isSortedAccordingTo(Comparator.comparing(this::getUsername))
        .hasSize(970);
  }

  @Test
  void testSortingUsernameDesc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.USERNAME_DESC))
        .isSortedAccordingTo(Comparator.comparing(this::getUsername).reversed())
        .hasSize(970);
  }

  @Test
  void testSortingTypeDesc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TYPE_DESC))
        .isSortedAccordingTo(Comparator.comparing(Event::getType).reversed())
        .hasSize(970);
  }

  @Test
  void testSortingTypeAsc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TYPE_ASC))
        .isSortedAccordingTo(Comparator.comparing(Event::getType))
        .hasSize(970);
  }
}
