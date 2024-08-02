package com.lemondead1.carshopservice.repo;

import static org.assertj.core.api.Assertions.*;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.service.LoggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRepoTest {
  private CarRepo cars;
  private UserRepo users;
  private OrderRepo orders;

  @BeforeEach
  void setup() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo(new LoggerService());
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
  }

  @Test
  void firstCreatedUserHasIdEqualToOne() {
    assertThat(users.create("username", "password", UserRole.CLIENT)).isEqualTo(1);
    assertThat(users.findById(1).id()).isEqualTo(1);
  }

  @Test
  void createdUserMatchesSpec() {
    users.create("user", "password", UserRole.CLIENT);
    users.create("admin", "password", UserRole.ADMIN);
    assertThat(users.findById(2)).isEqualTo(new User(2, "admin", "password", UserRole.ADMIN));
  }

  @Test
  void creatingUsersWithTheSameUsernameThrows() {
    users.create("user", "password", UserRole.CLIENT);
    assertThatThrownBy(() -> users.create("user", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void editedUserMatchesSpec() {
    users.create("user", "password", UserRole.CLIENT);
    users.edit(1).username("newUsername").password("newPassword").role(UserRole.ADMIN).apply();
    assertThat(users.findById(1)).isEqualTo(new User(1, "newUsername", "newPassword", UserRole.ADMIN));
  }

  @Test
  void editNotExistingUserThrows() {
    var builder = users.edit(1).username("username").password("password").role(UserRole.ADMIN);
    assertThatThrownBy(builder::apply).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void usernameConflictOnEditThrows() {
    users.create("user_1", "password", UserRole.CLIENT);
    users.create("user_2", "password", UserRole.ADMIN);
    var builder = users.edit(1).username("user_1").password("password").role(UserRole.ADMIN);
    assertThatThrownBy(builder::apply).isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void deleteReturnsOldUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.delete(1)).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void findByIdThrowsAfterDelete() {
    users.create("user", "password", UserRole.CLIENT);
    users.delete(1);
    assertThatThrownBy(() -> users.findById(1)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameThrowsAfterDelete() {
    users.create("user", "password", UserRole.CLIENT);
    users.delete(1);
    assertThatThrownBy(() -> users.findByUsername("user")).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameReturnsUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.findByUsername("user")).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void findByIdReturnsUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.findById(1)).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }
}
