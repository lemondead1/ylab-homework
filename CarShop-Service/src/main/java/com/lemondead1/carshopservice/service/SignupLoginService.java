package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import org.eclipse.jetty.security.LoginService;

public interface SignupLoginService extends LoginService {
  /**
   * Creates a new client user.
   *
   * @return Created user.
   * @throws UserAlreadyExistsException if there is a user with the given username.
   */
  User signUserUp(String username, String phoneNumber, String email, String password);
}
