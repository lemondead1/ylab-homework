package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.Builder;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;

public class UserRepo {
  @Setter
  private OrderRepo orders;

  private final Map<Integer, User> map = new HashMap<>();
  private final Map<String, User> usernameMap = new HashMap<>();
  private int lastId;

  public User create(String username, String password, UserRole role) {
    if (usernameMap.containsKey(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    lastId++;
    var newUser = new User(lastId, username, password, role);
    usernameMap.put(username, newUser);
    map.put(lastId, newUser);
    return newUser;
  }

  @Builder(builderMethodName = "", buildMethodName = "apply", builderClassName = "EditBuilder")
  private User applyEdit(int id, String username, String password, UserRole role) {
    var old = findById(id);

    password = password == null ? old.password() : password;
    role = role == null ? old.role() : role;
    if (username == null) {
      username = old.username();
    } else if (usernameMap.containsKey(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    var newUser = new User(id, username, password, role);
    map.put(id, newUser);
    usernameMap.remove(Objects.requireNonNull(old).username());
    usernameMap.put(username, newUser);
    return newUser;
  }

  public EditBuilder edit(int id) {
    return new EditBuilder().id(id);
  }

  public User delete(int id) {
    if (orders.existCustomerOrders(id)) {
      throw new ForeignKeyException("Cannot delete user " + id + " as there are orders referencing them.");
    }
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }
    usernameMap.remove(old.username());
    return old;
  }

  public boolean existsUsername(String username) {
    return usernameMap.containsKey(username);
  }

  public User findByUsername(String username) {
    if (!usernameMap.containsKey(username)) {
      throw new RowNotFoundException();
    }
    return usernameMap.get(username);
  }

  public User findById(int id) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException("User " + id + " does not exist.");
    }
    return map.get(id);
  }

  public List<User> search(@Nullable String username, @Nullable UserRole role, UserSorting sorting) {
    var stream = map.values().stream();
    if (username != null) {
      stream = stream.filter(user -> StringUtil.containsIgnoreCase(user.username(), username));
    }
    if (role != null) {
      stream = stream.filter(user -> user.role() == role);
    }
    stream = stream.sorted(switch (sorting) {
      case USERNAME_DESC -> Comparator.comparing(User::username, String::compareToIgnoreCase).reversed();
      case USERNAME_ASC -> Comparator.comparing(User::username, String::compareToIgnoreCase);
      case ROLE_DESC -> Comparator.comparing(User::role).reversed();
      case ROLE_ASC -> Comparator.comparing(User::role);
    });
    return stream.toList();
  }
}
