package com.lemondead1.carshopservice.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class EventRepo {
  private final DBManager db;
  private final ObjectMapper objectMapper;

  /**
   * Creates a new event
   *
   * @param timestamp event timestamp
   * @param userId    user who trigger the event
   * @param type      event type
   * @param json      auxiliary event data
   * @return the event created
   */
  public Event create(Instant timestamp, int userId, EventType type, Map<String, Object> json) {
    var sql = "insert into events (timestamp, user_id, type, data) values (?, ?, ?::event_type, ?::jsonb)";

    try (var stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setObject(1, timestamp.atOffset(ZoneOffset.UTC));
      stmt.setInt(2, userId);
      stmt.setString(3, type.getId());
      try {
        stmt.setString(4, objectMapper.writeValueAsString(json));
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException(e);
      }
      stmt.execute();

      var results = stmt.getGeneratedKeys();
      results.next();

      int id = results.getInt(1);

      return new Event(id, timestamp, userId, type, json);
    } catch (SQLException e) {
      throw new DBException("Failed to create a new event.", e);
    }
  }

  /**
   * Looks up events matching the query
   *
   * @param types    allowed event types
   * @param dates    date range
   * @param username username query
   * @param sorting  sorting
   * @return List of events matching the arguments
   */
  public List<Event> lookup(Set<EventType> types, Range<Instant> dates, String username, EventSorting sorting) {
    var sql = Util.format("""
                              select e.id, timestamp, user_id, type, data from events e
                              left join users u on u.id = e.user_id where
                              timestamp between coalesce(?, '-infinity'::timestamp) and coalesce(?, '+infinity'::timestamp) and
                              upper(coalesce(u.username, 'removed')) like '%' || upper(?) || '%' and
                              type in ({})
                              order by {}""",
                          Util.serializeSet(types),
                          getOrderingString(sorting));

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setObject(1, dates.min() == null ? null : dates.min().atOffset(ZoneOffset.UTC));
      stmt.setObject(2, dates.max() == null ? null : dates.max().atOffset(ZoneOffset.UTC));
      stmt.setString(3, username);

      stmt.execute();

      var results = stmt.getResultSet();

      List<Event> list = new ArrayList<>();

      while (results.next()) {
        list.add(readEvent(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException("Failed to lookup events.", e);
    }
  }

  private String getOrderingString(EventSorting sorting) {
    return switch (sorting) {
      case TIMESTAMP_DESC -> "timestamp desc";
      case TIMESTAMP_ASC -> "timestamp asc";
      case USERNAME_ASC -> "username asc";
      case USERNAME_DESC -> "username desc";
      case TYPE_ASC -> "type asc";
      case TYPE_DESC -> "type desc";
    };
  }

  private Event readEvent(ResultSet results) throws SQLException {
    var id = results.getInt(1);
    var timestamp = results.getObject(2, OffsetDateTime.class).toInstant();
    var userId = results.getInt(3);
    var type = EventType.parse(results.getString(4));
    var rawData = results.getString(5);

    Map<String, Object> data;
    try {
      data = objectMapper.readValue(rawData, new TypeReference<>() { });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return new Event(id, timestamp, userId, type, data);
  }
}
