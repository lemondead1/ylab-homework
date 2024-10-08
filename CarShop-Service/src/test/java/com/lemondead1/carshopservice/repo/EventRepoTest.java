package com.lemondead1.carshopservice.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.DBInitializer;
import com.lemondead1.carshopservice.HasIdEnumSetConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.RangeConverter;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = DBInitializer.class)
public class EventRepoTest {
  @Autowired
  ObjectMapper jackson;

  @Autowired
  EventRepo events;

  @Autowired
  UserRepo users;

  @Test
  @DisplayName("create creates an event.")
  void createEventTest() throws JsonProcessingException {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var json = jackson.readValue("{\"something\": \"doesnt_matter\", \"something_else\": \"value\"}",
                                 new TypeReference<Map<String, Object>>() { });
    var created = events.create(now, 1, EventType.USER_CREATED, json);
    assertThat(created)
        .isEqualTo(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_DESC).get(0))
        .isEqualTo(new Event(created.id(), now, 1, EventType.USER_CREATED, json));
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
        .map(Event::id).containsExactlyInAnyOrder(expectedIds);
  }

  @Test
  void testSortingTimestampDesc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_DESC))
        .isSortedAccordingTo(Comparator.comparing(Event::timestamp).reversed())
        .hasSize(970);
  }

  @Test
  void testSortingTimestampAsc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TIMESTAMP_ASC))
        .isSortedAccordingTo(Comparator.comparing(Event::timestamp))
        .hasSize(970);
  }

  String getUsername(Event event) {
    try {
      return users.findById(event.userId()).username();
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
        .isSortedAccordingTo(Comparator.comparing(Event::type).reversed())
        .hasSize(970);
  }

  @Test
  void testSortingTypeAsc() {
    assertThat(events.lookup(EventType.ALL_SET, Range.all(), "", EventSorting.TYPE_ASC))
        .isSortedAccordingTo(Comparator.comparing(Event::type))
        .hasSize(970);
  }
}
