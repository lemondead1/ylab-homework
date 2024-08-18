package com.lemondead1.carshopservice.servlet.users;

import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.UserService;
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

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCreationServletTest {
  @Mock
  UserService userService;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  MapStruct mapStruct = new MapStructImpl();

  UserCreationServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new UserCreationServlet(userService, mapStruct, jackson);
  }

  @Test
  @DisplayName("doPost calls createUser and writes the new user to writer")
  void doPostTest() throws IOException {
    var user = new User(100, "username", "81234567890", "user@example.com", "password", UserRole.CLIENT, 0);

    var requestBody = Util.format(
        "{\"username\": \"{}\", \"phone_number\": \"{}\", \"email\": \"{}\", \"password\": \"{}\", \"role\": \"{}\"}",
        user.username(), user.phoneNumber(), user.email(), user.password(), user.role().getId()
    );

    var responseBody = new StringWriter();

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
    when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    when(userService.createUser(user.username(), user.phoneNumber(), user.email(), user.password(), user.role()))
        .thenReturn(user);

    servlet.doPost(request, response);

    verify(response).setContentType("application/json");
    verify(response).setStatus(HttpStatus.CREATED_201);
    verify(userService).createUser(user.username(), user.phoneNumber(), user.email(), user.password(), user.role());
    assertThat(jackson.readValue(responseBody.toString(), ExistingUserDTO.class))
        .isEqualTo(mapStruct.userToUserDto(user));
  }
}
