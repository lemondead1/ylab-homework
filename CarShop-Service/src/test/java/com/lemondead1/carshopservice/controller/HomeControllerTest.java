package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {
  @Mock
  Runnable exitRunnable;

  MockCLI cli;

  HomeController home;

  @BeforeEach
  void setup() {
    home = new HomeController(exitRunnable);
    cli = new MockCLI();
  }

  @Test
  void exitCallsExitRunnable() {
    var dummyUser = new User(1, "username", "12346789", "mail@example.com", "pass", UserRole.ANONYMOUS, 0);

    home.exit(dummyUser, cli);

    cli.assertMatchesHistory();
    verify(exitRunnable).run();
  }
}
