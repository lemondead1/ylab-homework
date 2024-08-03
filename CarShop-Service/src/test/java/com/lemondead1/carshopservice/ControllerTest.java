package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.cli.CommandAcceptor;
import com.lemondead1.carshopservice.cli.ConsoleIO;
import com.lemondead1.carshopservice.cli.command.builders.CommandRootBuilder;
import com.lemondead1.carshopservice.controller.*;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Console;
import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ControllerTest {
  @Mock
  CarService cars;

  @Mock
  EventService events;

  @Mock
  OrderService orders;

  @Mock
  SessionService session;

  @Mock
  UserService users;

  @Mock
  Console scanner;

  @Mock
  Appendable out;

  CommandAcceptor acceptor;

  @BeforeEach
  void setup() {
    var builder = new CommandRootBuilder();
    new CarController(cars).registerEndpoints(builder);
    new EventController(events).registerEndpoints(builder);
    new HomeController().registerEndpoints(builder);
    new LoginController(users).registerEndpoints(builder);
    new OrderController(orders).registerEndpoints(builder);
    new UserController(users).registerEndpoints(builder);
    var rootCommand = builder.build();

    var cli = new ConsoleIO(scanner, out);

    acceptor = new CommandAcceptor(new BooleanSupplier() {
      boolean result = false;

      @Override
      public boolean getAsBoolean() {
        result ^= true;
        return result;
      }
    }, cli, session, rootCommand);
  }

  @SneakyThrows
  void verifyOut(String... expected) {
    var outCaptor = ArgumentCaptor.forClass(String.class);
    verify(out, atLeastOnce()).append(outCaptor.capture());
    assertThat(outCaptor.getAllValues()).containsExactly(expected);
  }

  @Test
  void loginCallsUserServiceLogin() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.ANONYMOUS);
    when(scanner.readLine()).thenReturn("login", "username", "password");
    acceptor.acceptCommands();
    verify(users).login("username", "password", session);
    verifyOut("> ", "Username > ", "Password > ", "Welcome, username!", "\n");
  }

  @Test
  void logoutSetsCurrentUserTo0() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.CLIENT);
    when(scanner.readLine()).thenReturn("logout");
    acceptor.acceptCommands();
    verify(session).setCurrentUserId(0);
    verifyOut("> ", "Logout", "\n");
  }

  @Test
  void signupCallsSignUserUp() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.ANONYMOUS);
    when(users.checkUsernameFree("username")).thenReturn(true);
    when(scanner.readLine()).thenReturn("signup", "username", "88005553535", "test@example.com", "password");
    acceptor.acceptCommands();
    verify(users).signUserUp("username", "88005553535", "test@example.com", "password");
    verifyOut("> ", "Username > ", "Phone number > ", "Email > ", "Password > ", "Signed up successfully!", "\n");
  }

  @Test
  void signupPrintsNameUsed() {
    when(session.getCurrentUserRole()).thenReturn(UserRole.ANONYMOUS);
    when(users.checkUsernameFree("username")).thenReturn(false);
    when(users.checkUsernameFree("newusername")).thenReturn(true);
    when(scanner.readLine()).thenReturn("signup", "username", "newusername", "88005553535", "test@example.com",
                                        "password");
    acceptor.acceptCommands();
    verify(users).signUserUp("newusername", "88005553535", "test@example.com", "password");
    verifyOut("> ", "Username > ", "Username 'username' is already taken.", "\n", "Username > ", "Phone number > ",
              "Email > ", "Password > ", "Signed up successfully!", "\n");
  }
}
