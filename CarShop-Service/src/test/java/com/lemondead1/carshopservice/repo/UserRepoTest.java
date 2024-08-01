package com.lemondead1.carshopservice.repo;

import static org.assertj.core.api.Assertions.*;

import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRepoTest {
  private UserRepo repo;

  @BeforeEach
  void setup() {
    repo = new UserRepo();
  }

  @Test
  void firstCreatedUserHasIdEqualToOne() {
    assertThat(repo.create("username", "password", UserRole.CLIENT)).isEqualTo(1);
    assertThat(repo.findById(1).id()).isEqualTo(1);
  }

  @Test
  void createdUserMatchesSpec() {
    repo.create("user", "password", UserRole.CLIENT);
    repo.create("admin", "password", UserRole.ADMIN);
    assertThat(repo.findById(2)).isEqualTo(new UserRepo.User(2, "admin", "password", UserRole.ADMIN));
  }

  @Test
  void creatingUsersWithTheSameUsernameThrows() {
    repo.create("user", "password", UserRole.CLIENT);
    assertThatThrownBy(() -> repo.create("user", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void editedUserMatchesSpec() {
    repo.create("user", "password", UserRole.CLIENT);
    repo.edit(1, "newUsername", "newPassword", UserRole.ADMIN);
    assertThat(repo.findById(1)).isEqualTo(new UserRepo.User(1, "newUsername", "newPassword", UserRole.ADMIN));
  }

  @Test
  void editNotExistingUserThrows() {
    assertThatThrownBy(() -> repo.edit(1, "username", "password", UserRole.ADMIN))
        .isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void usernameConflictOnEditThrows() {
    repo.create("user_1", "password", UserRole.CLIENT);
    repo.create("user_2", "password", UserRole.ADMIN);
    assertThatThrownBy(() -> repo.edit(2, "user_1", "password", UserRole.ADMIN))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void deleteReturnsOldUser() {
    repo.create("user", "password", UserRole.CLIENT);
    assertThat(repo.delete(1)).isEqualTo(new UserRepo.User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void findByIdThrowsAfterDelete() {
    repo.create("user", "password", UserRole.CLIENT);
    repo.delete(1);
    assertThatThrownBy(() -> repo.findById(1)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameThrowsAfterDelete() {
    repo.create("user", "password", UserRole.CLIENT);
    repo.delete(1);
    assertThatThrownBy(() -> repo.findByUsername("user")).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameReturnsUser() {
    repo.create("user", "password", UserRole.CLIENT);
    assertThat(repo.findByUsername("user")).isEqualTo(new UserRepo.User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void findByIdReturnsUser() {
    repo.create("user", "password", UserRole.CLIENT);
    assertThat(repo.findById(1)).isEqualTo(new UserRepo.User(1, "user", "password", UserRole.CLIENT));
  }
}
