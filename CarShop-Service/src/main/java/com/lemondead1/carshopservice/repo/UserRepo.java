package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.DBException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.Util;
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

  /**
   * Creates a new user
   *
   * @param username    username
   * @param phoneNumber phone number
   * @param email       email
   * @param password    password
   * @param role        role
   * @return the created user
   */
  public User create(String username, String phoneNumber, String email, String password, UserRole role) {
    var sql = "insert into users (username, phone_number, email, password, role) values (?, ?, ?, ?, ?::user_role)";

    try (var stmt = db.connect().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

  /**
   * Edits the user with the given id according to nonnull arguments
   *
   * @param userId      new user id
   * @param username    new username
   * @param phoneNumber new phone number
   * @param email       new email
   * @param password    new password
   * @param role        new role
   * @return New user row
   * @throws RowNotFoundException if a user with the given id does not exist
   */
  public User edit(int userId,
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
        (select count(*) from orders where client_id=users.id and kind='purchase' and state='done') as purchase_count""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.setString(2, phoneNumber);
      stmt.setString(3, email);
      stmt.setString(4, password);
      stmt.setString(5, role == null ? null : role.getId());
      stmt.setInt(6, userId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + userId + " does not exist.");
      }

      return readUser(results, 1);
    } catch (SQLException e) {
      throw new DBException("Failed to update user", e);
    }
  }

  /**
   * Deletes the user with the given id
   *
   * @param userId user id
   * @return The user that has been deleted
   * @throws RowNotFoundException if a user with the given id could not be found
   */
  public User delete(int userId) {
    var sql = "delete from users where id=? returning id, username, phone_number, email, password, role, 0";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, userId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + userId + " does not exist.");
      }

      return readUser(results, 1);
    } catch (SQLException e) {
      throw new DBException("Failed to delete user", e);
    }
  }

  /**
   * Checks whether a user with the given username exists
   *
   * @param username username
   * @return {@code true} if they exist, {@code false} otherwise
   */
  public boolean existsUsername(String username) {
    var sql = "select ? in (select username from users)";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.execute();

      var results = stmt.getResultSet();
      results.next();
      return results.getBoolean(1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  /**
   * Looks up a user which has the given username
   *
   * @param username username
   * @return A user who has the given username
   * @throws RowNotFoundException if such a user could not be found
   */
  public User findByUsername(String username) {
    var sql = """
        select id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=users.id and kind='purchase' and state='done') as purchase_count
        from users
        where username=?""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User with username '" + username + "' does not exist.");
      }

      return readUser(results, 1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  /**
   * Looks up a user by their id
   *
   * @param userId user id
   * @return The user who has the given id
   * @throws RowNotFoundException if there is no user with the given id
   */
  public User findById(int userId) {
    var sql = """
        select id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=users.id and kind='purchase' and state='done') as purchase_count
        from users
        where id=?""";

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setInt(1, userId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("User #" + userId + " does not exist.");
      }

      return readUser(results, 1);
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  /**
   * Looks up users by a query
   *
   * @param username      username query
   * @param roles         user roles (except anonymous)
   * @param phoneNumber   phone number query
   * @param email         email query
   * @param purchaseCount user purchase count range
   * @param sorting       resulting list sorting
   * @return List of users matching the query
   */
  public List<User> lookup(String username,
                           Set<UserRole> roles,
                           String phoneNumber,
                           String email,
                           IntRange purchaseCount,
                           UserSorting sorting) {
    String sql = Util.format("""
                                 select id, username, phone_number, email, password, role, purchase_count
                                 from (
                                   select id, username, phone_number, email, password, role,
                                          (select count(*) from orders where client_id=users.id and kind='purchase' and state='done') as purchase_count
                                   from users
                                 )
                                 where
                                 upper(username) like '%' || upper(?) || '%' and
                                 upper(phone_number) like '%' || upper(?) || '%' and
                                 upper(email) like '%' || upper(?) || '%' and
                                 purchase_count between ? and ? and
                                 role in ({})
                                 order by {}""",
                             Util.serializeSet(roles),
                             getOrderingString(sorting));

    try (var stmt = db.connect().prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.setString(2, phoneNumber);
      stmt.setString(3, email);
      stmt.setInt(4, purchaseCount.min());
      stmt.setInt(5, purchaseCount.max());
      stmt.execute();

      var results = stmt.getResultSet();

      List<User> list = new ArrayList<>();

      while (results.next()) {
        list.add(readUser(results, 1));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException("Failed to lookup users", e);
    }
  }

  private String getOrderingString(UserSorting sorting) {
    return switch (sorting) {
      case USERNAME_DESC -> "username desc";
      case USERNAME_ASC -> "username asc";
      case EMAIL_DESC -> "email desc";
      case EMAIL_ASC -> "email asc";
      case ROLE_DESC -> "role desc";
      case ROLE_ASC -> "role asc";
      case PURCHASES_DESC -> "purchase_count desc";
      case PURCHASES_ASC -> "purchase_count asc";
    };
  }

  static User readUser(ResultSet results, int startIndex) throws SQLException {
    var id = results.getInt(startIndex);
    var username = results.getString(startIndex + 1);
    var phoneNumber = results.getString(startIndex + 2);
    var email = results.getString(startIndex + 3);
    var password = results.getString(startIndex + 4);
    var role = UserRole.parse(results.getString(startIndex + 5));
    var purchaseCount = results.getInt(startIndex + 6);
    return new User(id, username, phoneNumber, email, password, role, purchaseCount);
  }
}
