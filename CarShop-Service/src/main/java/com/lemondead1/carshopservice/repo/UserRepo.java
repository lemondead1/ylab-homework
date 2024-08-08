package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.util.IntRange;
import com.lemondead1.carshopservice.util.StringUtil;
import lombok.Builder;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;

public class UserRepo {
  @Setter
  private OrderRepo orders;

  private record UserStore(int id, String username, String phoneNumber, String email, String password,
                           UserRole role) { }

  private final Map<Integer, UserStore> map = new HashMap<>();
  private final Map<String, UserStore> usernameMap = new HashMap<>();
  private int lastId;

  private User hydrate(UserStore store) {
    return new User(store.id(), store.username(), store.phoneNumber(), store.email(), store.password(), store.role(),
                    orders.getCustomerPurchaseCount(store.id()));
  }

  public User create(String username, String phoneNumber, String email, String password, UserRole role) {
    if (usernameMap.containsKey(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    lastId++;
    var newUser = new UserStore(lastId, username, phoneNumber, email, password, role);
    usernameMap.put(username, newUser);
    map.put(lastId, newUser);
    return hydrate(newUser);
  }

  @Builder(builderMethodName = "", buildMethodName = "apply", builderClassName = "EditBuilder")
  private User applyEdit(int id,
                         @Nullable String username,
                         @Nullable String phoneNumber,
                         @Nullable String email,
                         @Nullable String password,
                         @Nullable UserRole role) {
    var old = findById(id);

    phoneNumber = phoneNumber == null ? old.phoneNumber() : phoneNumber;
    email = email == null ? old.email() : email;
    password = password == null ? old.password() : password;
    role = role == null ? old.role() : role;
    if (username == null) {
      username = old.username();
    } else if (usernameMap.containsKey(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    var newUser = new UserStore(id, username, phoneNumber, email, password, role);
    map.put(id, newUser);
    usernameMap.remove(Objects.requireNonNull(old).username());
    usernameMap.put(username, newUser);
    return hydrate(newUser);
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
    return hydrate(old);
  }

  public boolean existsUsername(String username) {
    return usernameMap.containsKey(username);
  }

  public User findByUsername(String username) {
    if (!usernameMap.containsKey(username)) {
      throw new RowNotFoundException();
    }
    return hydrate(usernameMap.get(username));
  }

  public User findById(int id) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException("User " + id + " does not exist.");
    }
    return hydrate(map.get(id));
  }

  public List<User> lookup(String username, Set<UserRole> role, String phoneNumber, String email,
                           IntRange purchaseCount, UserSorting sorting) {
    return map.values()
              .stream()
              .map(this::hydrate)
              .filter(user -> purchaseCount.test(user.purchaseCount()))
              .filter(user -> StringUtil.containsIgnoreCase(user.username(), username))
              .filter(user -> StringUtil.containsIgnoreCase(user.phoneNumber(), phoneNumber))
              .filter(user -> StringUtil.containsIgnoreCase(user.email(), email))
              .filter(user -> role.contains(user.role()))
              .sorted(sorting.getSorter())
              .toList();
  }
}
