package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.SqlUtil;
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

  public Event create(Instant timestamp, int userId, EventType type, String json) {
    var sql = "insert into events (timestamp, user_id, type, data) values (?, ?, ?::event_type, ?::jsonb)";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

  public List<Event> lookup(Set<EventType> types, DateRange dates, String username, EventSorting sorting) {
    var template = """
        select id, timestamp, user_id, type, data from events e
        left join users u where u.id = e.user_id where
        timestamp between ? and ? and
        uppercase(u.username) like '%' || uppercase(?) || '%' and
        type in (%s)
        sorted by %s""";

    var sql = String.format(template, SqlUtil.serializeSet(types), switch (sorting) {
      case TIMESTAMP_DESC -> "timestamp desc";
      case TIMESTAMP_ASC -> "timestamp asc";
      case USERNAME_ASC -> "username asc";
      case USERNAME_DESC -> "username desc";
      case TYPE_ASC -> "type asc";
      case TYPE_DESC -> "type desc";
    });

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, dates.min());
      stmt.setObject(2, dates.max());
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

  private Event readEvent(ResultSet results) throws SQLException {
    var id = results.getInt(1);
    var timestamp = results.getObject(2, OffsetDateTime.class).toInstant();
    var userId = results.getInt(3);
    var type = EventType.parse(results.getString(4));
    var data = results.getString(5);
    return new Event(id, timestamp, userId, type, data);
  }
}
