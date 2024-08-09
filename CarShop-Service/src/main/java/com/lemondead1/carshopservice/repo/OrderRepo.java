package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.DateRange;
import com.lemondead1.carshopservice.util.SqlUtil;
import com.lemondead1.carshopservice.util.StringUtil;
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

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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
        (select count(*) from all_o where client_id=id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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
        (select count(*) from all_o where client_id=id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from all_o where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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
        u.id, u.username, u.phone_number, u.email, u.password, u.role, (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count,
        c.id, c.brand, c.model, c.production_year, c.price, c.condition, c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        from orders o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id
        where o.id=?""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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

  public boolean existCustomerOrders(int clientId) {
    var sql = "select ? in (select client_id from orders)";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  private String getOrdering(OrderSorting sorting) {
    return switch (sorting) {
      case LATEST_FIRST -> "o.created_at desc";
      case OLDEST_FIRST -> "o.created_at asc";
      case CAR_NAME_DESC -> "c.brand || ' ' || c.model desc";
      case CAR_NAME_ASC -> "c.brand || ' ' || c.model asc";
    };
  }

  /**
   * Fetches orders done by that customer.
   * Does not check whether the customer exists and in that case returns an empty list.
   *
   * @param customerId id of a customer
   * @return List of orders done by that customer
   */
  public List<Order> findCustomerOrders(int customerId, OrderSorting sorting) {
    var sql = StringUtil.format("""
                                    select
                                    o.id, o.created_at, o.kind, o.state, o.comment,
                                    u.id, u.username, u.phone_number, u.email, u.password, u.role,
                                    (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count,
                                    c.id, c.brand, c.model, c.production_year, c.price, c.condition,
                                    c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
                                    from orders o
                                    join users u on o.client_id=u.id
                                    join cars c on o.car_id=c.id
                                    where o.client_id=?
                                    order by {}""", getOrdering(sorting));

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, customerId);
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

  public List<Order> findCarOrders(int carId) {
    var sql = """
        select
        o.id, o.created_at, o.kind, o.state, o.comment,
        u.id, u.username, u.phone_number, u.email, u.password, u.role, (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count,
        c.id, c.brand, c.model, c.production_year, c.price, c.condition, c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        from orders o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id
        where o.car_id=?""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
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

  public List<Order> lookup(DateRange dates,
                            String customerName,
                            String carBrand,
                            String carModel,
                            Set<OrderKind> kinds,
                            Set<OrderState> states,
                            OrderSorting sorting) {
    var template = """
        select o.id, o.created_at, o.kind, o.state, o.comment,
        
        u.id, u.username, u.phone_number, u.email, u.password, u.role,
        (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count,
        
        c.id, c.brand, c.model, c.production_year, c.price, c.condition,
        c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        
        from orders o
        join users u on o.client_id=u.id
        join cars c on o.car_id=c.id
        where o.created_at between ? and ? and
        upper(u.username) like '%' || upper(?) || '%' and
        upper(c.brand) like '%' || upper(?) || '%' and
        upper(c.model) like '%' || upper(?) || '%' and
        o.state in (%s) and
        o.kind in (%s) and
        order by %s""";

    var sql = String.format(template, SqlUtil.serializeSet(states), SqlUtil.serializeSet(kinds), getOrdering(sorting));

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, dates.min());
      stmt.setObject(2, dates.max());
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

  private Order readOrder(ResultSet results) throws SQLException {
    var id = results.getInt(1);
    var createdAt = results.getObject(2, OffsetDateTime.class).toInstant();
    var kind = OrderKind.parse(results.getString(3));
    var state = OrderState.parse(results.getString(4));
    var comment = results.getString(5);
    var clientId = results.getInt(6);
    var username = results.getString(7);
    var phoneNumber = results.getString(8);
    var email = results.getString(9);
    var password = results.getString(10);
    var role = UserRole.parse(results.getString(11));
    var purchaseCount = results.getInt(12);
    var carId = results.getInt(13);
    var brand = results.getString(14);
    var model = results.getString(15);
    var productionYear = results.getInt(16);
    var price = results.getInt(17);
    var condition = results.getString(18);
    var availableForPurchase = results.getBoolean(19);
    var client = new User(clientId, username, phoneNumber, email, password, role, purchaseCount);
    var car = new Car(carId, brand, model, productionYear, price, condition, availableForPurchase);
    return new Order(id, createdAt, kind, state, client, car, comment);
  }

}
