package com.lemondead1.carshopservice.servlet.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.order.NewOrderDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.util.MapStruct;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.lemondead1.carshopservice.validation.Validated.validate;

@WebServlet(value = "/orders")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "client", "manager", "admin" }))
@RequiredArgsConstructor
public class OrderCreationServlet extends HttpServlet {
  private final OrderService orderService;
  private final MapStruct mapStruct;
  private final ObjectMapper objectMapper;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var currentUser = (User) req.getUserPrincipal();

    var newOrderDto = objectMapper.readValue(req.getReader(), NewOrderDTO.class);

    if (currentUser.role() == UserRole.CLIENT) {
      if (newOrderDto.clientId() != null && newOrderDto.clientId() != currentUser.id()) {
        throw new ForbiddenException("Clients cannot create orders for other users.");
      }
      if (newOrderDto.state() != null && newOrderDto.state() != OrderState.NEW) {
        throw new ForbiddenException("Clients cannot set order state.");
      }
    }

    var createdOrder = orderService.createOrder(
        validate(newOrderDto.clientId()).orElse(currentUser.id()),
        validate(newOrderDto.carId()).nonnull("Car id is required."),
        validate(newOrderDto.kind()).orElse(OrderKind.PURCHASE),
        validate(newOrderDto.state()).orElse(OrderState.NEW),
        validate(newOrderDto.comment()).orElse("")
    );

    resp.setStatus(HttpStatus.CREATED_201);
    resp.setContentType("application/json");
    var createdOrderDto = mapStruct.orderToOrderDto(createdOrder);
    objectMapper.writeValue(resp.getWriter(), createdOrderDto);
  }
}
