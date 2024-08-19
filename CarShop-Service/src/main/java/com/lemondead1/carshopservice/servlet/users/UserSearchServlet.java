package com.lemondead1.carshopservice.servlet.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.dto.user.UserQueryDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@WebServlet(value = "/users/search", asyncSupported = true)
@ServletSecurity(value = @HttpConstraint(rolesAllowed = { "manager", "admin" }))
@RequiredArgsConstructor
public class UserSearchServlet extends HttpServlet {
  private final UserService users;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserQueryDTO query = objectMapper.readValue(req.getReader(), UserQueryDTO.class);

    List<User> searchResult = users.lookupUsers(
        coalesce(query.username(), ""),
        coalesce(query.roles(), UserRole.ALL),
        coalesce(query.phoneNumber(), ""),
        coalesce(query.email(), ""),
        coalesce(query.purchases(), Range.all()),
        coalesce(query.sorting(), UserSorting.USERNAME_ASC)
    );

    List<ExistingUserDTO> searchResultDto = mapStruct.userToUserDtoList(searchResult);

    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getWriter(), searchResultDto);
  }
}
