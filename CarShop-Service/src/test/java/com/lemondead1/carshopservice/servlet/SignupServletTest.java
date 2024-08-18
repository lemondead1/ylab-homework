package com.lemondead1.carshopservice.servlet;

import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static com.lemondead1.carshopservice.ObjectMapperHolder.jackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SignupServletTest {
  @Mock
  SessionService session;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  MapStruct mapStruct = new MapStructImpl();

  SignupServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new SignupServlet(jackson, session, mapStruct);
  }

  @Test
  @DisplayName("doPost calls SessionService.signUserUp and writes new user to the writer stream.")
  void doPostCallsSignUserUpAndWritesNewUser() throws IOException {
    var user = new User(100, "username", "81234567890", "user@example.com", "password", UserRole.CLIENT, 0);

    var requestBody = Util.format(
        "{\"username\": \"{}\", \"phone_number\": \"{}\", \"email\": \"{}\", \"password\": \"{}\"}",
        user.username(), user.phoneNumber(), user.email(), user.password()
    );

    var responseBody = new StringWriter();

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
    when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    when(session.signUserUp(user.username(), user.phoneNumber(), user.email(), user.password())).thenReturn(user);

    servlet.doPost(request, response);

    verify(response).setContentType("application/json");
    verify(response).setStatus(HttpStatus.CREATED_201);
    verify(session).signUserUp(user.username(), user.phoneNumber(), user.email(), user.password());
    assertThat(jackson.readValue(responseBody.toString(), ExistingUserDTO.class))
        .isEqualTo(mapStruct.userToUserDto(user));
  }
}
