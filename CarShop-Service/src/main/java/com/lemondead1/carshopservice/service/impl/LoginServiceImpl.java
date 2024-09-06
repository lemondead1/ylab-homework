package com.lemondead1.carshopservice.service.impl;

import com.lemondead1.carshopservice.annotations.Timed;
import com.lemondead1.carshopservice.annotations.Transactional;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import com.lemondead1.carshopservice.repo.UserRepo;
import com.lemondead1.carshopservice.service.EventService;
import com.lemondead1.carshopservice.service.SignupLoginService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserIdentity;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Session;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@Timed
@Service
@RequiredArgsConstructor
public class LoginServiceImpl extends AbstractLoginService implements SignupLoginService {
  private final UserRepo users;
  private final EventService events;

  @Transactional
  @Override
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
  @Transactional
  public UserIdentity login(String username,
                            Object credentials,
                            Request request,
                            Function<Boolean, Session> getOrCreateSession) {
    UserIdentity login = super.login(username, credentials, request, getOrCreateSession);
    if (login == null) {
      return null;
    }
    events.onUserLoggedIn(((User) login.getUserPrincipal()).id());
    return login;
  }

  @Override
  protected List<RolePrincipal> loadRoleInfo(UserPrincipal user) {
    return List.of(((User) user).role().toPrincipal());
  }

  @Override
  @Nullable
  protected UserPrincipal loadUserInfo(String username) {
    try {
      return users.findByUsername(username);
    } catch (NotFoundException e) {
      return null;
    }
  }
}
