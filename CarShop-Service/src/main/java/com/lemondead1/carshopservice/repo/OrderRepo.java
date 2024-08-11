package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class OrderRepo {
  private final DBManager db;

  /**
   * Checks foreign key constraints and creates a new order
   *
   * @return the created order
   */
  public Order create(Instant createdAt, OrderKind kind, OrderState state, int clientId, int carId, String comments) {
    var sql = """
        with o as (
          insert into orders (created_at, kind, state, client_id, car_id, comment)
          values (?, ?::order_kind, ?::order_state, ?, ?, ?)
          returning id, created_at, kind, state, client_id, car_id, comment
        ),
        all_o as (
        	select * from orders union all select * from o
        )
        select o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from all_o where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setObject(1, createdAt.atOffset(ZoneOffset.UTC));
      stmt.setString(2, kind.getId());
      stmt.setString(3, state.getId());
      stmt.setInt(4, clientId);
      stmt.setInt(5, carId);
      stmt.setString(6, comments);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return readOrder(results);
    } catch (SQLException e) {
      throw new DBException("Failed to create order", e);
    }
  }

  public Order edit(int id,
                    @Nullable Instant createdAt,
                    @Nullable OrderKind kind,
                    @Nullable OrderState state,
                    @Nullable Integer clientId,
                    @Nullable Integer carId,
                    @Nullable String comments) {
    var sql = """
        with o as (
          update orders set created_at=coalesce(?, created_at),
                            kind=coalesce(?::order_kind, kind),
                            state=coalesce(?::order_state, state),
                            client_id=coalesce(?, client_id),
                            car_id=coalesce(?, car_id),
                            comment=coalesce(?, comment)
          where id=?
          returning id, created_at, kind, state, client_id, car_id, comment
        ),
        all_o as (
          select * from orders where id not in (select id from o) union all select * from o
        )
        select o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from all_o where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setObject(1, createdAt);
      stmt.setString(2, kind == null ? null : kind.getId());
      stmt.setString(3, state == null ? null : state.getId());
      stmt.setObject(4, clientId);
      stmt.setObject(5, carId);
      stmt.setString(6, comments);
      stmt.setInt(7, id);
      stmt.execute();

      var results = stmt.getResultSet();
      if (!results.next()) {
        throw new RowNotFoundException("Order #" + id + " not found.");
      }

      return readOrder(results);
    } catch (SQLException e) {
      throw new DBException("Failed to edit order", e);
    }
  }

  public Order delete(int id) {
    var sql = """
        with o as (
          delete from orders
          where id=?
          returning id, created_at, kind, state, client_id, car_id, comment
        ),
        all_o as (
          select * from orders where id not in (select id from o)
        )
        select o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from all_o where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return readOrder(results);
    } catch (SQLException e) {
      throw new DBException("Failed to delete order", e);
    }
  }

  public Order findById(int id) {
    var sql = """
        select
        o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from orders where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from orders o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id
        where o.id=?""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("Order #" + id + " not found.");
      }

      return readOrder(results);
    } catch (SQLException e) {
      throw new DBException("Failed to find order", e);
    }
  }

  public boolean doAnyOrdersExistFor(int clientId) {
    var sql = "select ? in (select client_id from orders)";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, clientId);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public boolean existCarOrders(int carId) {
    var sql = "select ? in (select car_id from orders)";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public int countClientOrders(int clientId) {
    var sql = "select count(*) from orders where client_id=?";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, clientId);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getInt(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  /**
   * Fetches orders done by that client.
   * Does not check whether the client exists and in that case returns an empty list.
   *
   * @param clientId id of a client
   * @return List of orders done by that client
   */
  public List<Order> findClientOrders(int clientId, OrderSorting sorting) {
    var sql = Util.format("""
                              select
                              o.id, o.created_at, o.kind, o.state, o.comment,
                              
                              u.id, u.username, u.phone_number, u.email, u.password, u.role,
                              (select count(*) from orders where client_id=u.id and kind='purchase' and state='done') as purchase_count,
                              
                              c.id, c.brand, c.model, c.production_year, c.price, c.condition,
                              c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
                              
                              from orders o
                              join users u on o.client_id=u.id
                              join cars c on o.car_id=c.id
                              where o.client_id=?
                              order by {}""", getOrderingString(sorting));

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, clientId);
      stmt.execute();

      List<Order> list = new ArrayList<>();

      var results = stmt.getResultSet();

      while (results.next()) {
        list.add(readOrder(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public void deleteClientOrders(int clientId) {
    var sql = "delete from orders where client_id=?";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, clientId);
      stmt.execute();
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public List<Order> deleteCarOrders(int carId) {
    var sql = """
        with o as (
          delete from orders
          where car_id=?
          returning id, created_at, kind, state, client_id, car_id, comment
        ),
        all_o as (
          select * from orders where id not in (select id from o)
        )
        select o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from all_o where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();

      List<Order> list = new ArrayList<>();

      while (results.next()) {
        list.add(readOrder(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public List<Order> findCarOrders(int carId) {
    var sql = """
        select
        o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from orders where client_id=u.id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from orders o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id
        where o.car_id=?""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      List<Order> list = new ArrayList<>();

      var results = stmt.getResultSet();

      while (results.next()) {
        list.add(readOrder(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public boolean doServiceOrdersExistFor(int clientId, int carId) {
    var sql = "select exists (select car_id from orders where kind='service' and car_id=? and client_id=?)";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.setInt(2, clientId);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public List<Order> lookup(DateRange dates,
                            String customerName,
                            String carBrand,
                            String carModel,
                            Set<OrderKind> kinds,
                            Set<OrderState> states,
                            OrderSorting sorting) {
    var sql = Util.format("""
                              select o.id, o.created_at, o.kind, o.state, o.comment,
                              
                              u.id, u.username, u.phone_number, u.email, u.password, u.role,
                              (select count(*) from orders where client_id=u.id and kind='purchase' and state='done') as purchase_count,
                              
                              c.id, c.brand, c.model, c.production_year, c.price, c.condition,
                              c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
                              
                              from orders o
                              join users u on o.client_id=u.id
                              join cars c on o.car_id=c.id
                              where o.created_at between ? and ? and
                              upper(u.username) like '%' || upper(?) || '%' and
                              upper(c.brand) like '%' || upper(?) || '%' and
                              upper(c.model) like '%' || upper(?) || '%' and
                              o.state in ({}) and
                              o.kind in ({})
                              order by {}""",
                          Util.serializeSet(states),
                          Util.serializeSet(kinds),
                          getOrderingString(sorting));

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setObject(1, dates.min().atOffset(ZoneOffset.UTC));
      stmt.setObject(2, dates.max().atOffset(ZoneOffset.UTC));
      stmt.setString(3, customerName);
      stmt.setString(4, carBrand);
      stmt.setString(5, carModel);
      stmt.execute();

      List<Order> list = new ArrayList<>();

      var results = stmt.getResultSet();

      while (results.next()) {
        list.add(readOrder(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  private String getOrderingString(OrderSorting sorting) {
    return switch (sorting) {
      case CREATED_AT_DESC -> "o.created_at desc";
      case CREATED_AT_ASC -> "o.created_at asc";
      case CAR_NAME_DESC -> "c.brand || ' ' || c.model desc";
      case CAR_NAME_ASC -> "c.brand || ' ' || c.model asc";
    };
  }

  static Order readOrder(ResultSet results) throws SQLException {
    var id = results.getInt(1);
    var createdAt = results.getObject(2, OffsetDateTime.class).toInstant();
    var kind = OrderKind.parse(results.getString(3));
    var state = OrderState.parse(results.getString(4));
    var comment = results.getString(5);
    var client = UserRepo.readUser(results, 6);
    var car = CarRepo.readCar(results, 13);
    return new Order(id, createdAt, kind, state, client, car, comment);
  }
}
