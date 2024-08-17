package com.lemondead1.carshopservice.servlet.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.user.NewUserDTO;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.cli.validation.Validated.validate;

@WebServlet("/users")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "admin" }))
@RequiredArgsConstructor
public class UserCreationServlet extends HttpServlet {
  private final UserService userService;
  private final MapStruct mapStruct;
  private final ObjectMapper objectMapper;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var newDto = objectMapper.readValue(req.getInputStream(), NewUserDTO.class);

    var createdUser = userService.createUser(
        validate(newDto.username()).by(Util.USERNAME).nonnull("Username is required."),
        validate(newDto.phoneNumber()).by(Util.PHONE_NUMBER).nonnull("Phone number is required."),
        validate(newDto.email()).by(Util.EMAIL).nonnull("Email is required."),
        validate(newDto.password()).by(Util.PASSWORD).nonnull("Password is required."),
        validate(newDto.role()).nonnull("Role is required.")
    );

    resp.setContentType("application/json");
    resp.setStatus(HttpStatus.CREATED_201);
    var createdDto = mapStruct.userToUserDto(createdUser);
    objectMapper.writeValue(resp.getOutputStream(), createdDto);
  }
}
