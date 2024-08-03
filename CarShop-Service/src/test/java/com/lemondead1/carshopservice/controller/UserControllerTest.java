package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
  @Mock
  UserService users;

  MockConsoleIO cli;

  UserController user;

  @BeforeEach
  void setup() {
    user = new UserController(users);

    cli = new MockConsoleIO();
  }

}
