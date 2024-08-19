package com.lemondead1.carshopservice.servlet.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.order.EditOrderDTO;
import com.lemondead1.carshopservice.dto.order.ExistingOrderDTO;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.OrderService;
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
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;

import static java.util.Optional.ofNullable;

@WebServlet("/orders/*")
@ServletSecurity(httpMethodConstraints = {
    @HttpMethodConstraint(value = "GET", rolesAllowed = { "client", "manager", "admin" }),
    @HttpMethodConstraint(value = "POST", rolesAllowed = { "client", "manager", "admin" }),
    @HttpMethodConstraint(value = "DELETE", rolesAllowed = { "manager", "admin" })
})
@RequiredArgsConstructor
public class OrdersByIdServlet extends HttpServlet {
  private final OrderService orderService;
  private final ObjectMapper objectMapper;
  private final MapStruct mapStruct;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int id = parseOrderId(req);
    User currentUser = (User) req.getUserPrincipal();

    Order order = orderService.findById(id);
    if (currentUser.role() == UserRole.CLIENT && order.client().id() != currentUser.id()) {
      throw new ForbiddenException("Clients cannot view other users' orders.");
    }

    resp.setContentType("application/json");
    ExistingOrderDTO orderDto = mapStruct.orderToOrderDto(order);
    objectMapper.writeValue(resp.getWriter(), orderDto);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int id = parseOrderId(req);
    User currentUser = (User) req.getUserPrincipal();
    EditOrderDTO editDto = objectMapper.readValue(req.getReader(), EditOrderDTO.class);

    Order editedOrder;

    if (currentUser.role() == UserRole.CLIENT) {
      var toEdit = orderService.findById(id);
      if (toEdit.client().id() != currentUser.id()) {
        throw new ForbiddenException("Clients cannot cancel other users' orders.");
      }
      if (editDto.state() != OrderState.CANCELLED) {
        throw new ForbiddenException("Clients can only cancel their orders.");
      }
      editedOrder = orderService.cancel(id, ofNullable(editDto.appendComment()).map(c -> "\n" + c).orElse(""));
    } else {
      editedOrder = orderService.updateState(id,
                                             editDto.state(),
                                             ofNullable(editDto.appendComment()).map(c -> "\n" + c).orElse(""));
    }

    resp.setContentType("application/json");
    ExistingOrderDTO editedDto = mapStruct.orderToOrderDto(editedOrder);
    objectMapper.writeValue(resp.getWriter(), editedDto);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    int id = parseOrderId(req);
    orderService.deleteOrder(id);
    resp.setStatus(HttpStatus.NO_CONTENT_204);
  }

  @VisibleForTesting
  int parseOrderId(HttpServletRequest req) {
    String[] split = req.getPathInfo().split("/");
    if (split.length < 2) {
      throw new BadRequestException("Order id must be specified.");
    }
    try {
      return Integer.parseInt(split[1]);
    } catch (NumberFormatException e) {
      throw new BadRequestException(Util.format("'{}' is not an integer.", split[1]));
    }
  }
}
