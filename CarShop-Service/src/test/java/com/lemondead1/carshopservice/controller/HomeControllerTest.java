package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.service.SessionService;
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

  @Mock
  SessionService session;

  MockConsoleIO cli;

  HomeController home;

  @BeforeEach
  void setup() {
    home = new HomeController(exitRunnable);
    cli = new MockConsoleIO();
  }

  @Test
  void exitCallsExitRunnable() {
    home.exit(session, cli);

    cli.assertMatchesHistory();
    verify(exitRunnable).run();
  }
}
