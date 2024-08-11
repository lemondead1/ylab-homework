package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.DateRangeConverter;
import com.lemondead1.carshopservice.HasIdEnumSetConverter;
import com.lemondead1.carshopservice.IntegerArrayConverter;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.DateRange;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EventRepoTest {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres").withReuse(true);

  static DBManager dbManager;
  static EventRepo events;
  static UserRepo users;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    dbManager = new DBManager(postgres.getJdbcUrl(), postgres.getUsername(),
                              postgres.getPassword(), "data", "infra", true);
    dbManager.setupDatabase();
    events = new EventRepo(dbManager);
    users = new UserRepo(dbManager);
  }

  @AfterAll
  static void afterAll() {
    dbManager.dropSchemas();
  }

  @Test
  @DisplayName("create creates an event.")
  void createEventTest() {
    var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var json = "{\"something\": \"doesnt_matter\", \"something_else\": \"value\"}";
    var created = events.create(now, 1, EventType.USER_CREATED, json);
    assertThat(created)
        .isEqualTo(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC).get(0))
        .isEqualTo(new Event(created.id(), now, 1, EventType.USER_CREATED, json));
  }

  @Nested
  class LookupTest {
    @BeforeAll
    static void beforeAll() {
      dbManager.dropSchemas();
      dbManager.setupDatabase();
    }

    @ParameterizedTest
    @CsvSource({
        "'225, 227, 238, 241, 261, 262, 263', user_edited, 3.7.2014 - 10.7.2014, ''",
        "'240, 255',                          ALL,         3.7.2014 - 10.7.2014, admin",
    })
    @DisplayName("lookup returns entries matching arguments.")
    void filterTest(@ConvertWith(IntegerArrayConverter.class) Integer[] expectedIds,
                    @ConvertWith(HasIdEnumSetConverter.class) Set<EventType> types,
                    @ConvertWith(DateRangeConverter.class) DateRange dates,
                    String username) {
      assertThat(events.lookup(types, dates, username, EventSorting.USERNAME_DESC))
          .map(Event::id).containsExactlyInAnyOrder(expectedIds);
    }

    @Test
    void testSortingTimestampDesc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.TIMESTAMP_DESC))
          .isSortedAccordingTo(Comparator.comparing(Event::timestamp).reversed())
          .hasSize(970);
    }

    @Test
    void testSortingTimestampAsc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.TIMESTAMP_ASC))
          .isSortedAccordingTo(Comparator.comparing(Event::timestamp))
          .hasSize(970);
    }

    String getUsername(Event event) {
      try {
        return users.findById(event.userId()).username();
      } catch (RowNotFoundException e) {
        return "removed";
      }
    }

    @Test
    void testSortingUsernameAsc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.USERNAME_ASC))
          .isSortedAccordingTo(Comparator.comparing(this::getUsername))
          .hasSize(970);
    }

    @Test
    void testSortingUsernameDesc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.USERNAME_DESC))
          .isSortedAccordingTo(Comparator.comparing(this::getUsername).reversed())
          .hasSize(970);
    }

    @Test
    void testSortingTypeDesc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.TYPE_DESC))
          .isSortedAccordingTo(Comparator.comparing(Event::type).reversed())
          .hasSize(970);
    }

    @Test
    void testSortingTypeAsc() {
      assertThat(events.lookup(EventType.ALL_SET, DateRange.ALL, "", EventSorting.TYPE_ASC))
          .isSortedAccordingTo(Comparator.comparing(Event::type))
          .hasSize(970);
    }
  }
}
