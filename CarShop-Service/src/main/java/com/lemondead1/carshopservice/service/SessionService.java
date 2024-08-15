package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.security.CustomUserPrincipal;
import com.lemondead1.carshopservice.util.MapStruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserIdentity;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Session;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class SessionService extends AbstractLoginService {
  private static final User anonymous = new User(0, "anonymous", "+71234567890", "test@example.com",
                                                 "password", UserRole.ANONYMOUS, 0);

  private final MapStruct mapStruct;
  private final UserRepo users;
  private final EventService events;
  private int currentUserId;

  public User getCurrentUser() {
    if (currentUserId == 0) {
      return anonymous;
    }
    try {
      return users.findById(currentUserId);
    } catch (RowNotFoundException e) {
      currentUserId = 0;
      return anonymous;
    }
  }

  public boolean checkUsernameFree(String username) {
    return !users.existsUsername(username);
  }

  public User signUserUp(String username, String phoneNumber, String email, String password) {
    if (users.existsUsername(username)) {
      throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
    }
    var user = users.create(username, phoneNumber, email, password, UserRole.CLIENT);
    events.onUserSignedUp(user);
    return user;
  }

  @Override
  @Nullable
  public UserIdentity login(String username,
                            Object credentials,
                            Request request,
                            Function<Boolean, Session> getOrCreateSession) {
    var login = super.login(username, credentials, request, getOrCreateSession);
    if (login == null) {
      return null;
    }
    events.onUserLoggedIn(((CustomUserPrincipal) login.getUserPrincipal()).getId());
    return login;
  }

  @Override
  protected List<RolePrincipal> loadRoleInfo(UserPrincipal user) {
    return List.of(((CustomUserPrincipal) user).getRole());
  }

  @Override
  @Nullable
  protected UserPrincipal loadUserInfo(String username) {
    try {
      return mapStruct.userToCustomUserPrincipal(users.findByUsername(username));
    } catch (RowNotFoundException e) {
      return null;
    }
  }
}
