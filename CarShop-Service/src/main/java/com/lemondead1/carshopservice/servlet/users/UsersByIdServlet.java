package com.lemondead1.carshopservice.servlet.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.user.NewUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.exceptions.MethodNotAllowedException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.cli.validation.Validated.validate;

@WebServlet(value = "/users/*", asyncSupported = true)
@ServletSecurity(httpMethodConstraints = {
    @HttpMethodConstraint(value = "GET", rolesAllowed = { "client", "manager", "admin" }),
    @HttpMethodConstraint(value = "POST", rolesAllowed = "admin"),
    @HttpMethodConstraint(value = "DELETE", rolesAllowed = "admin")
})
@RequiredArgsConstructor
public class UsersByIdServlet extends HttpServlet {
  private final UserService userService;
  private final OrderService orderService;
  private final MapStruct mapStruct;
  private final ObjectMapper objectMapper;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var params = parseQueryParams(req);
    var currentUser = (User) req.getUserPrincipal();

    User user;
    if (params.userId() == currentUser.id()) {
      user = currentUser;
    } else if (currentUser.role() == UserRole.CLIENT) {
      throw new ForbiddenException("Clients can only query their own profiles.");
    } else {
      user = userService.findById(params.userId());
    }

    resp.setContentType("application/json");

    if (params.orders) {
      var sortingString = req.getParameter("sorting");

      OrderSorting sorting;

      if (sortingString == null) {
        sorting = OrderSorting.CREATED_AT_DESC;
      } else {
        try {
          sorting = OrderSorting.parse(sortingString);
        } catch (IllegalArgumentException e) {
          throw new BadRequestException(sortingString + " is not recognized.");
        }
      }

      var orders = orderService.findClientOrders(params.userId(), sorting);
      var ordersDto = mapStruct.orderListToDtoList(orders);
      objectMapper.writeValue(resp.getOutputStream(), ordersDto);
    } else {
      var userDto = mapStruct.userToUserDto(user);
      objectMapper.writeValue(resp.getOutputStream(), userDto);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var params = parseQueryParams(req);

    if (params.orders()) {
      throw new MethodNotAllowedException("Method PATCH is not allowed on user order list.");
    }

    var userDto = objectMapper.readValue(req.getInputStream(), NewUserDTO.class);

    var user = userService.editUser(
        params.userId(),
        validate(userDto.username()).by(Util.USERNAME).orNull(),
        validate(userDto.phoneNumber()).by(Util.PHONE_NUMBER).orNull(),
        validate(userDto.email()).by(Util.EMAIL).orNull(),
        validate(userDto.password()).by(Util.PASSWORD).orNull(),
        userDto.role()
    );

    var returnUserDto = mapStruct.userToUserDto(user);
    resp.setContentType("application/json");
    objectMapper.writeValue(resp.getOutputStream(), returnUserDto);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    var params = parseQueryParams(req);

    if (params.orders()) {
      throw new MethodNotAllowedException("Method DELETE is not allowed on user order list.");
    }

    boolean cascade = "true".equalsIgnoreCase(req.getParameter("cascade"));
    if (cascade) {
      userService.deleteUserCascading(params.userId());
    } else {
      userService.deleteUser(params.userId());
    }
    resp.setStatus(HttpStatus.NO_CONTENT_204);
  }

  private record QueryPath(int userId, boolean orders) { }

  private QueryPath parseQueryParams(HttpServletRequest req) {
    var split = req.getPathInfo().split("/");

    boolean orders = false;
    switch (split.length) {
      case 0, 1:
        throw new BadRequestException("User id must be specified.");
      case 3:
        if ("orders".equals(split[2])) {
          orders = true;
        } else {
          throw new NotFoundException("User has only orders endpoint.");
        }
      case 2:
        int userId;
        if ("me".equals(split[1])) {
          userId = ((User) req.getUserPrincipal()).id();
        } else {
          try {
            userId = Integer.parseInt(split[1]);
          } catch (NumberFormatException e) {
            throw new BadRequestException("'" + split[1] + " is not an integer");
          }
        }
        return new QueryPath(userId, orders);
      default:
        throw new NotFoundException("This endpoint does not exist.");
    }
  }
}
