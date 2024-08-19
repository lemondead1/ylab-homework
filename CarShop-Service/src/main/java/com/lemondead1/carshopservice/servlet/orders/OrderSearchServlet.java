package com.lemondead1.carshopservice.servlet.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.order.ExistingOrderDTO;
import com.lemondead1.carshopservice.dto.order.OrderQueryDTO;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.service.OrderService;
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
import java.util.Optional;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@WebServlet(value = "/orders/search")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "manager", "admin" }))
@RequiredArgsConstructor
public class OrderSearchServlet extends HttpServlet {
  private final OrderService orderService;
  private final MapStruct mapStruct;
  private final ObjectMapper objectMapper;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    OrderQueryDTO queryDto = objectMapper.readValue(req.getReader(), OrderQueryDTO.class);

    List<Order> result = orderService.lookupOrders(
        coalesce(queryDto.dates(), Range.all()),
        coalesce(queryDto.username(), ""),
        coalesce(queryDto.carBrand(), ""),
        coalesce(queryDto.carModel(), ""),
        Optional.ofNullable(queryDto.kind()).map(List::of).orElse(OrderKind.ALL),
        coalesce(queryDto.state(), OrderState.ALL),
        coalesce(queryDto.sorting(), OrderSorting.CREATED_AT_DESC)
    );

    resp.setContentType("application/json");
    List<ExistingOrderDTO> resultDto = mapStruct.orderListToDtoList(result);
    objectMapper.writeValue(resp.getWriter(), resultDto);
  }
}
