package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.SqlUtil;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class UserRepo {
  private final DBManager db;

  public User create(String username, String phoneNumber, String email, String password, UserRole role) {
    var sql = "insert into users (username, phone_number, email, password, role) values (?, ?, ?, ?, ?::user_role)";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, username);
      stmt.setString(2, phoneNumber);
      stmt.setString(3, email);
      stmt.setString(4, password);
      stmt.setString(5, role.getId());
      stmt.execute();

      var results = stmt.getGeneratedKeys();
      results.next();

      var id = results.getInt(1);

      return new User(id, username, phoneNumber, email, password, role, 0);
    } catch (SQLException e) {
      throw new DBException("Failed to create a user.", e);
    }
  }

  public User edit(int id,
                   @Nullable String username,
                   @Nullable String phoneNumber,
                   @Nullable String email,
                   @Nullable String password,
                   @Nullable UserRole role) {
    var sql = """
        update users set username=coalesce(?, username),
                         phone_number=coalesce(?, phone_number),
                         email=coalesce(?, email),
                         password=coalesce(?, password),
                         role=coalesce(?::user_role, role)
        where id=?
        returning id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.setString(2, phoneNumber);
      stmt.setString(3, email);
      stmt.setString(4, password);
      stmt.setString(5, role == null ? null : role.getId());
      stmt.setInt(6, id);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + id + " does not exist.");
      }

      return readUser(results);
    } catch (SQLException e) {
      throw new DBException("Failed to update user", e);
    }
  }

  public User delete(int id) {
    var sql = """
        delete from users where id=?
        returning id, username, phone_number, email, password, role, 0""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + id + " does not exist.");
      }

      return readUser(results);
    } catch (SQLException e) {
      throw new DBException("Failed to delete user", e);
    }
  }

  public boolean existsUsername(String username) {
    var sql = "select ? in (select username from users)";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public User findByUsername(String username) {
    var sql = """
        select id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count
        from users
        where username=?""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User with username '" + username + "' does not exist.");
      }

      return readUser(results);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public User findById(int id) {
    var sql = """
        select id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count
        from users
        where id=?""";

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + id + " does not exist.");
      }

      return readUser(results);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public List<User> lookup(String username,
                           Set<UserRole> roles,
                           String phoneNumber,
                           String email,
                           IntRange purchaseCount,
                           UserSorting sorting) {
    String sql = StringUtil.format("""
                                       select id, username, phone_number, email, password, role, purchase_count
                                       from (
                                         select id, username, phone_number, email, password, role,
                                                (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count
                                         from users
                                       )
                                       where
                                       upper(username) like '%' || upper(?) || '%' and
                                       upper(phone_number) like '%' || upper(?) || '%' and
                                       upper(email) like '%' || upper(?) || '%' and
                                       purchase_count between ? and ? and
                                       role in ({})
                                       order by {}""",
                                   SqlUtil.serializeSet(roles),
                                   switch (sorting) {
                                     case USERNAME_DESC -> "username desc";
                                     case USERNAME_ASC -> "username asc";
                                     case EMAIL_DESC -> "email desc";
                                     case EMAIL_ASC -> "email asc";
                                     case ROLE_DESC -> "role desc";
                                     case ROLE_ASC -> "role asc";
                                     case PURCHASES_DESC -> "purchase_count desc";
                                     case PURCHASES_ASC -> "purchase_count asc";
                                   });

    try (var conn = db.connect(); var stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.setString(2, phoneNumber);
      stmt.setString(3, email);
      stmt.setInt(4, purchaseCount.min());
      stmt.setInt(5, purchaseCount.max());
      stmt.execute();

      var results = stmt.getResultSet();

      List<User> list = new ArrayList<>();

      while (results.next()) {
        list.add(readUser(results));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException("Failed to lookup users", e);
    }
  }

  private User readUser(ResultSet results) throws SQLException {
    var id = results.getInt(1);
    var username = results.getString(2);
    var phoneNumber = results.getString(3);
    var email = results.getString(4);
    var password = results.getString(5);
    var role = UserRole.parse(results.getString(6));
    var purchaseCount = results.getInt(7);
    return new User(id, username, phoneNumber, email, password, role, purchaseCount);
  }

}
