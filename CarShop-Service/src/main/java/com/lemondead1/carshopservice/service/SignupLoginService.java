package com.lemondead1.carshopservice.service;

import com.lemondead1.carshopservice.entity.User;
import org.eclipse.jetty.security.LoginService;

public interface SignupLoginService extends LoginService {
  User signUserUp(String username, String phoneNumber, String email, String password);
}
