package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class EventRepo {
  private final DBManager db;

  /**
   * Creates a new event
   *
   * @param timestamp event timestamp
   * @param userId    user who trigger the event
   * @param type      event type
   * @param json      auxiliary event data
   * @return the event created
   */
  public Event create(Instant timestamp, int userId, EventType type, String json) {
    var sql = "insert into events (timestamp, user_id, type, data) values (?, ?, ?::event_type, ?::jsonb)";

    try (var stmt = db.connect().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setObject(1, timestamp.atOffset(ZoneOffset.UTC));
      stmt.setInt(2, userId);
      stmt.setString(3, type.getId());
      stmt.setString(4, json);
      stmt.execute();

      var results = stmt.getGeneratedKeys();
      results.next();

      var id = results.getInt(1);

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
  public List<Event> lookup(Set<EventType> types, DateRange dates, String username, EventSorting sorting) {
    var sql = Util.format("""
                              select e.id, timestamp, user_id, type, data from events e
                              left join users u on u.id = e.user_id where
                              timestamp between ? and ? and
                              upper(coalesce(u.username, 'removed')) like '%' || upper(?) || '%' and
                              type in ({})
                              order by {}""",
                          Util.serializeSet(types),
                          getOrderingString(sorting));

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setObject(1, dates.min().atOffset(ZoneOffset.UTC));
      stmt.setObject(2, dates.max().atOffset(ZoneOffset.UTC));
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
    var data = results.getString(5);
    return new Event(id, timestamp, userId, type, data);
  }
}
