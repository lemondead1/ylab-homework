package com.lemondead1.carshopservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.SignupDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.service.SessionService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.validation.Validated.validate;

@WebServlet(value = "/signup", asyncSupported = true)
@RequiredArgsConstructor
public class SignupServlet extends HttpServlet {
  private final ObjectMapper objectMapper;
  private final SessionService sessionService;
  private final MapStruct mapStruct;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    SignupDTO signup = objectMapper.readValue(req.getReader(), SignupDTO.class);

    User user = sessionService.signUserUp(
        validate(signup.username()).by(Util.USERNAME).nonnull("Username is required"),
        validate(signup.phoneNumber()).by(Util.PHONE_NUMBER).nonnull("Phone number is required"),
        validate(signup.email()).by(Util.EMAIL).nonnull("Email is required"),
        validate(signup.password()).by(Util.PASSWORD).nonnull("Password is required")
    );

    ExistingUserDTO userDto = mapStruct.userToUserDto(user);

    resp.setStatus(HttpStatus.CREATED_201);
    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getWriter(), userDto);
  }
}
