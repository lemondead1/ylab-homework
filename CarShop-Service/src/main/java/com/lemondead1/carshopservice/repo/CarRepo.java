package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.database.DBManager;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.CarSorting;
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
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class CarRepo {
  private final DBManager db;

  /**
   * Creates a new car
   *
   * @param brand          brand
   * @param model          model
   * @param productionYear production year
   * @param price          price
   * @param condition      condition
   * @return car that was created
   */
  public Car create(String brand, String model, int productionYear, int price, String condition) {
    var sql = "insert into cars (brand, model, production_year, price, condition) values (?, ?, ?, ?, ?)";

    try (var stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, brand);
      stmt.setString(2, model);
      stmt.setInt(3, productionYear);
      stmt.setInt(4, price);
      stmt.setString(5, condition);
      stmt.execute();

      var generatedKeys = stmt.getGeneratedKeys();
      generatedKeys.next();

      var generatedId = generatedKeys.getInt(1);

      return new Car(generatedId, brand, model, productionYear, price, condition, true);
    } catch (SQLException e) {
      throw new DBException("Failed to create a car", e);
    }
  }

  /**
   * Changes the car with given id according to nonnull parameters.
   *
   * @param carId          id
   * @param brand          new brand
   * @param model          new model
   * @param productionYear new production year
   * @param price          new price
   * @param condition      new condition
   * @return edited row
   */
  public Car edit(int carId,
                  @Nullable String brand,
                  @Nullable String model,
                  @Nullable Integer productionYear,
                  @Nullable Integer price,
                  @Nullable String condition) {
    var sql = """
        update cars c set brand=coalesce(?, brand),
                          model=coalesce(?, model),
                          production_year=coalesce(?, production_year),
                          price=coalesce(?, price),
                          condition=coalesce(?, condition)
        where id=?
        returning id, brand, model, production_year, price, condition,
                  c.id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase""";

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setString(1, brand);
      stmt.setString(2, model);
      stmt.setObject(3, productionYear);
      stmt.setObject(4, price);
      stmt.setString(5, condition);
      stmt.setInt(6, carId);

      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("Car #" + carId + " not found.");
      }

      return readCar(results, 1);
    } catch (SQLException e) {
      throw new DBException("Failed to update a car", e);
    }
  }

  /**
   * Validates foreign key constraints and deletes the car
   *
   * @param carId id to be deleted
   * @return old row
   * @throws RowNotFoundException if a car with given id does not exist.
   */
  public Car delete(int carId) {
    var sql = """
        delete from cars where id=?
        returning id, brand, model, production_year, price, condition,
                  id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase""";

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("Car #" + carId + " not found.");
      }

      return readCar(results, 1);
    } catch (SQLException e) {
      throw new DBException("Failed to update a car", e);
    }
  }

  /**
   * Finds a car by its id.
   *
   * @param carId id to find
   * @return a car with the given id
   * @throws RowNotFoundException if the car was not found
   */
  public Car findById(int carId) {
    var sql = """
        select id, brand, model, production_year, price, condition,
               id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
        from cars where id=?""";

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        throw new RowNotFoundException("Car #" + carId + " not found.");
      }

      return readCar(results, 1);
    } catch (SQLException e) {
      throw new DBException("Failed to find a car.", e);
    }
  }

  /**
   * Returns a user that has a purchase order with status 'done' and the given car id in their name.
   *
   * @param carId car id
   * @return {@code some(user)} if such an order exists, {@code empty()} otherwise
   */
  public Optional<User> findCarOwner(int carId) {
    var sql = """
        select u.id, username, phone_number, email, password, role,
        (select count(*) from orders where client_id=id and kind='purchase' and state='done') as purchase_count
        from users u
        join orders o on o.client_id=u.id
        where o.car_id=? and o.state='done' and o.kind='purchase'""";

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setInt(1, carId);
      stmt.execute();

      var results = stmt.getResultSet();

      if (!results.next()) {
        return Optional.empty();
      }

      return Optional.of(UserRepo.readUser(results, 1));
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  /**
   * Looks up cars matching query
   *
   * @param brand car brand query
   * @param model car model query
   * @param productionYear production year range
   * @param price price range
   * @param condition car condition query
   * @param availabilityForPurchase availability for purchase
   * @param sorting sorting
   * @return List of cars matching the query
   */
  public List<Car> lookup(String brand,
                          String model,
                          IntRange productionYear,
                          IntRange price,
                          String condition,
                          Set<Boolean> availabilityForPurchase,
                          CarSorting sorting) {
    var sql = Util.format("""
                              select id, brand, model, production_year, price, condition, available_for_purchase
                              from (
                                select id, brand, model, production_year, price, condition,
                                       id not in (select car_id from orders where state!='cancelled' and kind='purchase') as available_for_purchase
                                from cars
                              )
                              where upper(brand) like '%' || upper(?) || '%' and
                                    upper(model) like '%' || upper(?) || '%' and
                                    production_year between ? and ? and
                                    price between ? and ? and
                                    upper(condition) like '%' || upper(?) || '%' and
                                    available_for_purchase in ({})
                                    order by {}""",
                          Util.serializeBooleans(availabilityForPurchase),
                          getOrderingString(sorting));

    try (var stmt = db.getConnection().prepareStatement(sql)) {
      stmt.setString(1, brand);
      stmt.setString(2, model);
      stmt.setInt(3, productionYear.min());
      stmt.setInt(4, productionYear.max());
      stmt.setInt(5, price.min());
      stmt.setInt(6, price.max());
      stmt.setString(7, condition);
      stmt.execute();

      List<Car> list = new ArrayList<>();

      var results = stmt.getResultSet();

      while (results.next()) {
        list.add(readCar(results, 1));
      }

      return list;
    } catch (SQLException e) {
      throw new DBException("Failed to lookup database.", e);
    }
  }

  private String getOrderingString(CarSorting sorting) {
    return switch (sorting) {
      case NAME_ASC -> "brand || ' ' || model asc";
      case NAME_DESC -> "brand || ' ' || model desc";
      case PRODUCTION_YEAR_ASC -> "production_year asc";
      case PRODUCTION_YEAR_DESC -> "production_year desc";
      case PRICE_ASC -> "price asc";
      case PRICE_DESC -> "price desc";
    };
  }

  static Car readCar(ResultSet results, int startIndex) throws SQLException {
    var id = results.getInt(startIndex);
    var brand = results.getString(startIndex + 1);
    var model = results.getString(startIndex + 2);
    var productionYear = results.getInt(startIndex + 3);
    var price = results.getInt(startIndex + 4);
    var condition = results.getString(startIndex + 5);
    var availableForPurchase = results.getBoolean(startIndex + 6);
    return new Car(id, brand, model, productionYear, price, condition, availableForPurchase);
  }
}
